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
import com.example.spendsense.CurrencyHelper
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
import kotlin.collections.ArrayList

class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }

    private var fullTransactionList: List<Transaction> = emptyList()
    private var currentFilter = "week" // week, month, all

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

    override fun onResume() {
        super.onResume()
        if (fullTransactionList.isNotEmpty()) {
            applyFilter(currentFilter)
        }
    }

    private fun observeViewModel() {
        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            transactions?.let {
                fullTransactionList = it
                applyFilter(currentFilter)
            }
        })
    }

    private fun setupFilterButtons() {
        updateFilterButtonStyles(currentFilter)
        binding.btnThisWeek.setOnClickListener { applyFilter("week") }
        binding.btnThisMonth.setOnClickListener { applyFilter("month") }
        binding.btnAllTime.setOnClickListener { applyFilter("all") }
    }

    private fun applyFilter(filter: String) {
        currentFilter = filter
        updateFilterButtonStyles(filter)

        val calendar = Calendar.getInstance()

        val filteredTransactions = when (filter) {
            "week" -> {
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                fullTransactionList.filter { parseDate(it.date)?.after(calendar.time) ?: false }
            }
            "month" -> {
                calendar.add(Calendar.MONTH, -1)
                fullTransactionList.filter { parseDate(it.date)?.after(calendar.time) ?: false }
            }
            else -> fullTransactionList
        }
        updateAllCharts(filteredTransactions)
    }

    private fun updateFilterButtonStyles(selectedFilter: String) {
        val buttons = listOf(binding.btnThisWeek, binding.btnThisMonth, binding.btnAllTime)
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)

        buttons.forEach {
            it.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            it.setTextColor(primaryColor)
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
        setupLineChart(filteredTransactions) // THIS WAS MISSING
        setupTopCategories(filteredTransactions)
    }

    private fun updateSummaryCards(filteredTransactions: List<Transaction>) {
        val totalSpending = filteredTransactions.filter { it.type == "expense" }.sumOf { it.amount }
        val currency = CurrencyHelper.getCurrencySymbol(requireContext())

        binding.tvTotalSpending.text = "$currency${String.format("%.0f", totalSpending)}"
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

        val data = PieData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val currency = CurrencyHelper.getCurrencySymbol(requireContext())
                    return "$currency${value.toInt()}"
                }
            })
        }

        binding.pieChart.data = data
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
            colors = listOf(ContextCompat.getColor(requireContext(), R.color.income_green), ContextCompat.getColor(requireContext(), R.color.expense_red))
            valueTextSize = 12f
        }

        val data = BarData(dataSet).apply {
            setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val currency = CurrencyHelper.getCurrencySymbol(requireContext())
                    return "$currency${value.toInt()}"
                }
            })
            barWidth = 0.4f
        }

        binding.barChart.data = data
        binding.barChart.description.isEnabled = false
        binding.barChart.legend.isEnabled = false
        binding.barChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(listOf("Income", "Expense"))
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
        }
        binding.barChart.axisLeft.axisMinimum = 0f
        binding.barChart.axisRight.isEnabled = false
        binding.barChart.animateY(1000)
        binding.barChart.invalidate()
    }

    // --- THIS IS THE NEW FUNCTION THAT WAS MISSING ---
    private fun setupLineChart(filteredTransactions: List<Transaction>) {
        val expenses = filteredTransactions.filter { it.type == "expense" }

        if (expenses.isEmpty()) {
            binding.lineChart.clear()
            binding.lineChart.setNoDataText("No expense data available")
            return
        }

        // Group expenses by date and sum them up
        val dailyExpenses = expenses.groupBy { it.date }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .toSortedMap()

        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        var index = 0f

        dailyExpenses.forEach { (date, amount) ->
            entries.add(Entry(index, amount.toFloat()))

            // Format date label (e.g., "Jan 15")
            val formattedDate = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
                val d = inputFormat.parse(date)
                outputFormat.format(d ?: Date())
            } catch (e: Exception) { date }
            labels.add(formattedDate)

            index++
        }

        val dataSet = LineDataSet(entries, "Daily Spending").apply {
            color = ContextCompat.getColor(requireContext(), R.color.expense_red)
            valueTextColor = if (isAdded) ContextCompat.getColor(requireContext(), R.color.black) else Color.BLACK
            lineWidth = 2f
            setCircleColor(ContextCompat.getColor(requireContext(), R.color.expense_red))
            circleRadius = 4f
            setDrawFilled(true)
            fillColor = ContextCompat.getColor(requireContext(), R.color.expense_red)
            fillAlpha = 50
            valueTextSize = 10f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            setDrawValues(false)
        }

        // Value formatter to add Currency symbol to tooltips
        dataSet.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                val currency = CurrencyHelper.getCurrencySymbol(requireContext())
                return "$currency${value.toInt()}"
            }
        }

        binding.lineChart.data = LineData(dataSet)
        binding.lineChart.description.isEnabled = false
        binding.lineChart.legend.isEnabled = false

        binding.lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(labels)
            labelRotationAngle = -45f
            textColor = if (isAdded) ContextCompat.getColor(requireContext(), R.color.black) else Color.BLACK
        }

        binding.lineChart.axisLeft.apply {
            setDrawGridLines(true)
            axisMinimum = 0f
            textColor = if (isAdded) ContextCompat.getColor(requireContext(), R.color.black) else Color.BLACK
        }

        binding.lineChart.axisRight.isEnabled = false
        binding.lineChart.setExtraOffsets(10f, 10f, 10f, 20f) // Add padding for labels
        binding.lineChart.animateX(1000)
        binding.lineChart.invalidate()
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
        Color.parseColor("#FFA726"), Color.parseColor("#AB47BC"), Color.parseColor("#26A69A"),
        Color.parseColor("#FFCA28"), Color.parseColor("#5C6BC0")
    )

    private fun parseDate(dateString: String): Date? = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
    } catch (e: Exception) { null }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}