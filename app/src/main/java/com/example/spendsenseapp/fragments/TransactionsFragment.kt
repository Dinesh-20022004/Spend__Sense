package com.example.spendsense.fragments

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense.*
import com.example.spendsense.adapters.TransactionAdapter
import com.example.spendsense.databinding.FragmentTransactionsBinding
import com.example.spendsense.models.Transaction
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory

class TransactionsFragment : Fragment() {

    private var _binding: FragmentTransactionsBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }

    private lateinit var transactionAdapter: TransactionAdapter
    private var allTransactions = listOf<Transaction>()
    private var currentFilter = "all"
    private var currentSearchQuery = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
    }

    override fun onResume() {
        super.onResume()
        // 1. Force the adapter to refresh its view holders (to pick up the new currency symbol)
        if (::transactionAdapter.isInitialized) {
            transactionAdapter.notifyDataSetChanged()
        }

        // 2. Re-apply the current filter.
        // This will call updateUI() which recalculates the total at the top using the new currency.
        applyFilter(currentFilter, allTransactions)
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            transactions = emptyList(),
            onItemClick = { transaction -> editTransaction(transaction) },
            onDeleteClick = { transaction -> confirmDelete(transaction) }
        )
        binding.rvTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun observeViewModel() {
        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            transactions?.let {
                allTransactions = it
                applyFilter(currentFilter, it)
            }
        })
    }

    private fun setupClickListeners() {
        binding.btnAll.setOnClickListener { applyFilter("all", allTransactions) }
        binding.btnIncome.setOnClickListener { applyFilter("income", allTransactions) }
        binding.btnExpense.setOnClickListener { applyFilter("expense", allTransactions) }

        binding.ivSearch.setOnClickListener {
            if (binding.searchView.visibility == View.VISIBLE) {
                val slideUp = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
                binding.searchView.startAnimation(slideUp)
                binding.searchView.visibility = View.GONE
                if (currentSearchQuery.isNotEmpty()) {
                    binding.searchView.setQuery("", false)
                }
            } else {
                binding.searchView.visibility = View.VISIBLE
                val slideDown = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
                binding.searchView.startAnimation(slideDown)
                binding.searchView.requestFocus()
            }
        }

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText.orEmpty()
                applyFilter(currentFilter, allTransactions)
                return true
            }
        })
    }

    private fun applyFilter(filter: String, transactions: List<Transaction>) {
        currentFilter = filter
        updateFilterButtonStyles(filter)

        val typeFilteredList = when (currentFilter) {
            "income" -> transactions.filter { it.type == "income" }
            "expense" -> transactions.filter { it.type == "expense" }
            else -> transactions
        }

        val finalList = if (currentSearchQuery.isNotEmpty()) {
            typeFilteredList.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true)
            }
        } else {
            typeFilteredList
        }

        updateUI(finalList)
    }

    private fun updateFilterButtonStyles(selectedFilter: String) {
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val whiteColor = ContextCompat.getColor(requireContext(), R.color.white)
        val transparentColor = ContextCompat.getColor(requireContext(), android.R.color.transparent)

        binding.btnAll.apply { setBackgroundColor(transparentColor); setTextColor(primaryColor) }
        binding.btnIncome.apply { setBackgroundColor(transparentColor); setTextColor(ContextCompat.getColor(requireContext(), R.color.income_green)) }
        binding.btnExpense.apply { setBackgroundColor(transparentColor); setTextColor(ContextCompat.getColor(requireContext(), R.color.expense_red)) }

        when (selectedFilter) {
            "all" -> binding.btnAll.apply { setBackgroundColor(primaryColor); setTextColor(whiteColor) }
            "income" -> binding.btnIncome.apply { setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.income_green)); setTextColor(whiteColor) }
            "expense" -> binding.btnExpense.apply { setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.expense_red)); setTextColor(whiteColor) }
        }
    }

    private fun updateUI(transactionsToDisplay: List<Transaction>) {
        transactionAdapter.updateTransactions(transactionsToDisplay)

        val currency = CurrencyHelper.getCurrencySymbol(requireContext())

        if (transactionsToDisplay.isEmpty()) {
            binding.rvTransactions.visibility = View.GONE
            binding.llEmptyState.visibility = View.VISIBLE
            binding.tvCount.text = "0 transactions"
            binding.tvTotal.text = "Total: ${currency}0.00"
        } else {
            binding.rvTransactions.visibility = View.VISIBLE
            binding.llEmptyState.visibility = View.GONE

            val count = transactionsToDisplay.size
            binding.tvCount.text = "$count transaction${if (count != 1) "s" else ""}"

            var total = 0.0
            for (transaction in transactionsToDisplay) {
                if (transaction.type == "income") {
                    total += transaction.amount
                } else {
                    total -= transaction.amount
                }
            }
            binding.tvTotal.text = "Total: $currency${String.format("%.2f", total)}"
        }
    }

    private fun editTransaction(transaction: Transaction) {
        val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("editing_transaction", transaction)
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    private fun confirmDelete(transaction: Transaction) {
        AlertDialog.Builder(requireContext())
            .setTitle("Delete Transaction")
            .setMessage("Are you sure you want to delete '${transaction.title}'?")
            .setPositiveButton("Delete") { _, _ ->
                transactionViewModel.delete(transaction)
                Toast.makeText(requireContext(), "Transaction deleted", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}