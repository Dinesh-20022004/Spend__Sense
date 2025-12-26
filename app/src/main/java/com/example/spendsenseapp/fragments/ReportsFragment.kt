package com.example.spendsense.fragments

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense.R
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.adapters.CategoryStatAdapter
import com.example.spendsense.databinding.FragmentReportsBinding
import com.example.spendsense.models.CategoryStat
import com.example.spendsense.models.Transaction
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import java.text.SimpleDateFormat
import java.util.*

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    // Get the shared ViewModel instance using the factory.
    // NEW AND CORRECT
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }
    private var currentFilter = "week" // "week", "month", or "all"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterButtons()
        observeViewModel() // The new entry point for data.
    }

    private fun observeViewModel() {
        // This block will execute automatically whenever the transaction list in the database changes.
        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { allTransactions ->
            allTransactions?.let {
                // When new data arrives, re-apply the current date filter.
                applyFilter(currentFilter, it)
            }
        })
    }

    private fun setupFilterButtons() {
        updateFilterButtonStyles(currentFilter)
        val allTransactions = transactionViewModel.allTransactions.value ?: emptyList()
        binding.btnThisWeek.setOnClickListener { applyFilter("week", allTransactions) }
        binding.btnThisMonth.setOnClickListener { applyFilter("month", allTransactions) }
        binding.btnAllTime.setOnClickListener { applyFilter("all", allTransactions) }
    }

    private fun applyFilter(filter: String, allTransactions: List<Transaction>) {
        currentFilter = filter
        updateFilterButtonStyles(filter)

        val calendar = Calendar.getInstance()

        val filteredTransactions = when (filter) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                allTransactions.filter { parseDate(it.date)?.after(calendar.time) ?: false }
            }
            "month" -> {
                calendar.add(Calendar.MONTH, -1)
                allTransactions.filter { parseDate(it.date)?.after(calendar.time) ?: false }
            }
            else -> allTransactions
        }
        updateAllCharts(filteredTransactions)
    }

    private fun updateFilterButtonStyles(selectedFilter: String) {
        val buttons = listOf(binding.btnThisWeek, binding.btnThisMonth, binding.btnAllTime)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)

        buttons.forEach { button ->
            button.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            button.setTextColor(primaryColor)
        }

        val selectedButton = when (selectedFilter) {
            "week" -> binding.btnThisWeek
            "month" -> binding.btnThisMonth
            else -> binding.btnAllTime
        }
        selectedButton.setBackgroundColor(primaryColor)
        selectedButton.setTextColor(Color.WHITE)
    }

    private fun updateAllCharts(filteredTransactions: List<Transaction>) {
        updateSummaryCards(filteredTransactions)
        setupPieChart(filteredTransactions)
        setupBarChart(filteredTransactions)
        setupTopCategories(filteredTransactions)
    }

    private fun updateSummaryCards(filteredTransactions: List<Transaction>) {
        val totalSpending = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        binding.tvTotalSpending.text = "₹${String.format("%.0f", totalSpending)}"
        binding.tvTransactionCount.text = "${filteredTransactions.size}"
    }

    private fun setupPieChart(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }

        if (expenses.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            binding.tvNoDataPie.visibility = View.VISIBLE
            return
        }
        binding.pieChart.visibility = View.VISIBLE
        binding.tvNoDataPie.visibility = View.GONE

        val categoryMap = expenses.groupBy { it.category }.mapValues { entry -> entry.value.sumOf { it.amount } }
        val entries = categoryMap.map { PieEntry(it.value.toFloat(), it.key) }

        val dataSet = PieDataSet(entries, "").apply {
            colors = getChartColors()
            valueTextSize = 10f
            valueTextColor = Color.WHITE
        }

        binding.pieChart.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            isDrawHoleEnabled = true
            holeRadius = 45f
            setEntryLabelColor(Color.BLACK)
            animateY(1000, Easing.EaseInOutQuad)
            invalidate()
        }
    }

    private fun setupBarChart(filteredTransactions: List<Transaction>) {
        val income = filteredTransactions.filter { it.type == "income" }.sumOf { it.amount }
        val expense = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        val entries = listOf(BarEntry(0f, income.toFloat()), BarEntry(1f, expense.toFloat()))

        val dataSet = BarDataSet(entries, "").apply {
            colors = listOf(
                ContextCompat.getColor(requireContext(), R.color.income_green),
                ContextCompat.getColor(requireContext(), R.color.expense_red)
            )
            valueTextSize = 12f
        }

        binding.barChart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            legend.isEnabled = false
            xAxis.valueFormatter = IndexAxisValueFormatter(listOf("Income", "Expense"))
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            axisLeft.axisMinimum = 0f
            axisRight.isEnabled = false
            animateY(1000)
            invalidate()
        }
    }

    private fun setupTopCategories(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }
        if (expenses.isEmpty()) {
            binding.rvTopCategories.visibility = View.GONE
            return
        }
        binding.rvTopCategories.visibility = View.VISIBLE
        val totalExpense = expenses.sumOf { it.amount }
        if (totalExpense == 0.0) {
            binding.rvTopCategories.visibility = View.GONE
            return
        }

        val categoryStats = expenses.groupBy { it.category }
            .map { CategoryStat(it.key, it.value.sumOf { t -> t.amount }, it.value.size, (it.value.sumOf { t -> t.amount } / totalExpense) * 100) }
            .sortedByDescending { it.amount }.take(5)

        binding.rvTopCategories.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTopCategories.adapter = CategoryStatAdapter(categoryStats)
    }

    private fun getChartColors(): List<Int> = listOf(
        Color.parseColor("#EF5350"), Color.parseColor("#42A5F5"), Color.parseColor("#66BB6A"),
        Color.parseColor("#FFA726"), Color.parseColor("#AB47BC"), Color.parseColor("#26A69A")
    )

    private fun parseDate(dateString: String): Date? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
    } catch (e: Exception) { null }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}