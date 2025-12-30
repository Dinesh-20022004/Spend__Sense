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

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }

    // NEW: Store the full list locally to ensure filters always have data to work with
    private var fullTransactionList: List<Transaction> = emptyList()
    private var currentFilter = "week" // Default filter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupFilterButtons()
        observeViewModel()
    }

    private fun observeViewModel() {
        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            transactions?.let {
                // Update our local copy of the full list
                fullTransactionList = it
                // Re-apply the current filter with the new data
                applyFilter(currentFilter)
            }
        })
    }

    private fun setupFilterButtons() {
        // Just call applyFilter with the string. It will use the local fullTransactionList.
        binding.btnThisWeek.setOnClickListener { applyFilter("week") }
        binding.btnThisMonth.setOnClickListener { applyFilter("month") }
        binding.btnAllTime.setOnClickListener { applyFilter("all") }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateFilterButtonStyles(filter)

        val calendar = Calendar.getInstance()
        // Reset time to midnight so we compare dates only, not times
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        val filteredTransactions = when (filter) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weekAgo = calendar.time
                fullTransactionList.filter {
                    val transDate = parseDate(it.date)
                    // Include if date is NOT before the cutoff (so it includes the cutoff day)
                    transDate != null && !transDate.before(weekAgo)
                }
            }
            "month" -> {
                calendar.add(Calendar.MONTH, -1)
                val monthAgo = calendar.time
                fullTransactionList.filter {
                    val transDate = parseDate(it.date)
                    transDate != null && !transDate.before(monthAgo)
                }
            }
            else -> fullTransactionList // "all" returns everything
        }

        updateAllCharts(filteredTransactions)
    }

    private fun updateFilterButtonStyles(selectedFilter: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val transparentColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        binding.btnThisWeek.apply { setBackgroundColor(transparentColor); setTextColor(primaryColor) }
        binding.btnThisMonth.apply { setBackgroundColor(transparentColor); setTextColor(primaryColor) }
        binding.btnAllTime.apply { setBackgroundColor(transparentColor); setTextColor(primaryColor) }

        when (selectedFilter) {
            "week" -> binding.btnThisWeek.apply { setBackgroundColor(primaryColor); setTextColor(whiteColor) }
            "month" -> binding.btnThisMonth.apply { setBackgroundColor(primaryColor); setTextColor(whiteColor) }
            "all" -> binding.btnAllTime.apply { setBackgroundColor(primaryColor); setTextColor(whiteColor) }
        }
    }

    private fun updateAllCharts(filteredTransactions: List<Transaction>) {
        // If the list is empty, we still want to update UI to show empty states
        updateSummaryCards(filteredTransactions)
        setupPieChart(filteredTransactions)
        setupBarChart(filteredTransactions)
        setupTopCategories(filteredTransactions)
        setupLineChart(filteredTransactions)
    }

    private fun updateSummaryCards(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }
        val totalSpending = expenses.sumOf { it.amount }

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

        binding.pieChart.data = PieData(dataSet)
        binding.pieChart.description.isEnabled = false
        binding.pieChart.legend.isEnabled = false
        binding.pieChart.isDrawHoleEnabled = true
        binding.pieChart.holeRadius = 45f
        binding.pieChart.setEntryLabelColor(Color.BLACK)
        binding.pieChart.animateY(1000, Easing.EaseInOutQuad)
        binding.pieChart.invalidate()
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

        binding.barChart.data = BarData(dataSet)
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("Income", "Expense"))
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
        }
        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    private fun setupLineChart(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }

        // Group expenses by date
        val dailyExpenses = expenses.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toSortedMap()

        val entries = ArrayList<Entry>()
        var index = 0f
        dailyExpenses.forEach { (_, amount) ->
            entries.add(Entry(index, amount.toFloat()))
            index++
        }

        if (entries.isEmpty()) {
            binding.lineChart.clear() // Clear if no data
            return
        }

        val dataSet = LineDataSet(entries, "Daily Spending").apply {
            color = ContextCompat.getColor(requireContext(), R.color.expense_red)
            valueTextColor = Color.BLACK
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.expense_red))
            setDrawValues(false)
        }

        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.description.isEnabled = false
        binding.lineChart.legend.isEnabled = false
        binding.lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            setDrawLabels(false) // Hide date labels for simplicity
        }
        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.animateX(1000)
        binding.lineChart.invalidate()
    }

    private fun setupTopCategories(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }

        // Always set visibility. If empty, hide it.
        if (expenses.isEmpty()) {
            binding.rvTopCategories.visibility = View.GONE
            return
        }
        binding.rvTopCategories.visibility = View.VISIBLE

        val totalExpense = expenses.sumOf { it.amount }
        // Prevent division by zero
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

    private fun parseDate(dateString: String): Date? {
        // Ensure this matches the format you save in AddTransactionActivity (yyyy-MM-dd)
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) { null }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}