package com.example.spendsense.fragments

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense.R
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.adapters.BudgetAdapter
import com.example.spendsense.databinding.DialogAddBudgetBinding
import com.example.spendsense.databinding.FragmentBudgetBinding
import com.example.spendsense.models.Budget
import com.example.spendsense.models.Transaction
import com.example.spendsense.viewmodels.BudgetViewModel
import com.example.spendsense.viewmodels.BudgetViewModelFactory
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class BudgetFragment : Fragment() {

    private var _binding: FragmentBudgetBinding? = null
    private val binding get() = _binding!!

    // NEW AND CORRECT
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }
    private val budgetViewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(requireActivity().application)
    }

    private lateinit var budgetAdapter: BudgetAdapter
    private var currentBudgets = listOf<Budget>()

    private val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Education", "Other")

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentBudgetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClickListeners()
        observeViewModels()
    }

    private fun setupRecyclerView() {
        budgetAdapter = BudgetAdapter(emptyList()) { budget ->
            showDeleteDialog(budget)
        }
        binding.rvBudgets.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = budgetAdapter
        }
    }

    private fun observeViewModels() {
        val currentMonth = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())

        budgetViewModel.getBudgetsForMonth(currentMonth).observe(viewLifecycleOwner, Observer { budgets ->
            this.currentBudgets = budgets
            calculateSpentAmounts(transactionViewModel.allTransactions.value ?: emptyList())
            updateUI()
        })

        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            calculateSpentAmounts(transactions)
            updateUI()
        })
    }

    private fun setupClickListeners() {
        binding.ivAddBudget.setOnClickListener {
            showAddBudgetDialog()
        }
    }

    private fun calculateSpentAmounts(transactions: List<Transaction>) {
        val currentMonthTransactions = transactions.filter { isSameMonth(it.date) }
        currentBudgets.forEach { it.spent = 0.0 }

        for (budget in currentBudgets) {
            val spentForCategory = currentMonthTransactions
                .filter { it.category == budget.category && it.type == "expense" }
                .sumOf { it.amount }
            budget.spent = spentForCategory
        }
    }

    private fun isSameMonth(dateString: String): Boolean {
        try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val date = dateFormat.parse(dateString) ?: return false
            val calendar = Calendar.getInstance()
            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            calendar.time = date
            return currentYear == calendar.get(Calendar.YEAR) && currentMonth == calendar.get(Calendar.MONTH)
        } catch (e: Exception) { return false }
    }

    private fun updateUI() {
        val totalBudget = currentBudgets.sumOf { it.amount }
        val totalSpent = currentBudgets.sumOf { it.spent }
        val overallPercentage = if (totalBudget > 0) (totalSpent / totalBudget) * 100 else 0.0

        binding.tvTotalBudget.text = "₹${String.format("%.0f", totalBudget)}"
        binding.tvTotalSpent.text = "₹${String.format("%.0f", totalSpent)}"
        binding.progressOverall.progress = overallPercentage.toInt().coerceIn(0, 100)
        binding.tvOverallPercentage.text = "${String.format("%.0f", overallPercentage)}%"

        val overallRemaining = totalBudget - totalSpent

        val overallColorRes = when {
            overallPercentage >= 100 -> R.color.expense_red
            overallPercentage >= 80 -> android.R.color.holo_orange_dark
            else -> R.color.income_green
        }
        val colorInt = ContextCompat.getColor(requireContext(), overallColorRes)
        binding.progressOverall.progressTintList = ColorStateList.valueOf(colorInt)
        binding.tvRemaining.setTextColor(colorInt)

        if (overallRemaining < 0) {
            binding.tvRemaining.text = "⚠️ Over budget by ₹${String.format("%.0f", -overallRemaining)}"
        } else {
            binding.tvRemaining.text = "₹${String.format("%.0f", overallRemaining)} remaining"
        }

        if (currentBudgets.isEmpty()) {
            binding.llEmptyState.visibility = View.VISIBLE
            binding.rvBudgets.visibility = View.GONE
        } else {
            binding.llEmptyState.visibility = View.GONE
            binding.rvBudgets.visibility = View.VISIBLE
            budgetAdapter.updateBudgets(currentBudgets.sortedByDescending { it.getPercentage() })
        }
    }

    private fun showAddBudgetDialog() {
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_add_budget, null)
        val bindingDialog = DialogAddBudgetBinding.bind(dialogView)

        val existingCategories = currentBudgets.map { it.category }
        val availableCategories = expenseCategories.filter { !existingCategories.contains(it) }

        if (availableCategories.isEmpty()) {
            Toast.makeText(requireContext(), "All expense categories already have a budget", Toast.LENGTH_SHORT).show()
            return
        }

        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, availableCategories)
        bindingDialog.actvCategory.setAdapter(adapter)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Budget")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val category = bindingDialog.actvCategory.text.toString()
                val amount = bindingDialog.etAmount.text.toString().toDoubleOrNull()

                if (category.isNotEmpty() && amount != null && amount > 0) {
                    val newBudget = Budget(
                        id = 0, // This tells Room to auto-generate the ID for a new entry.
                        category = category,
                        amount = amount,
                        month = SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
                    )
                    budgetViewModel.insert(newBudget)
                    Toast.makeText(requireContext(), "Budget for '$category' added!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Please fill all fields correctly", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showDeleteDialog(budget: Budget) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Budget")
            .setMessage("Are you sure you want to delete the budget for '${budget.category}'?")
            .setPositiveButton("Delete") { _, _ ->
                budgetViewModel.delete(budget)
                Toast.makeText(requireContext(), "Budget deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}