package com.example.spendsense.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spendsense.*
import com.example.spendsense.adapters.TransactionAdapter
import com.example.spendsense.databinding.FragmentHomeBinding
import com.example.spendsense.models.Transaction
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Get the shared ViewModel instance using the factory.
    // NEW AND CORRECT
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }

    private lateinit var transactionAdapter: TransactionAdapter

    // The ActivityResultLauncher for handling the notification permission result.
    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                NotificationHelper.showSampleNotification(requireContext())
            } else {
                Toast.makeText(requireContext(), "Notification permission denied.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel() // The new entry point for data.
    }

    // onResume() is no longer needed to refresh data. LiveData handles this automatically.

    private fun setupUI() {
        // This part still uses SharedPreferences for the user's name, which is fine for profile data.
        val userAccountsPrefs = requireActivity().getSharedPreferences("UserAccounts", Context.MODE_PRIVATE)
        val loggedInEmail = UserSessionManager.getLoggedInEmail(requireContext())

        val storedData = userAccountsPrefs.getString(loggedInEmail, "User|")
        val name = storedData?.split("|")?.get(0) ?: "User"
        binding.tvUserName.text = name

        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        binding.tvGreeting.text = when (hour) {
            in 0..11 -> "Good Morning"
            in 12..16 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionAdapter(
            transactions = emptyList(),
            onItemClick = { transaction -> openEditTransaction(transaction) },
            onDeleteClick = {
                Toast.makeText(requireContext(), "Go to Transactions tab to delete", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvRecentTransactions.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = transactionAdapter
        }
    }

    private fun observeViewModel() {
        // This is the core of the reactive architecture.
        // This block will execute automatically whenever the data in the 'transactions' table changes.
        transactionViewModel.allTransactions.observe(viewLifecycleOwner, Observer { transactions ->
            transactions?.let {
                // Update the balance cards with the full list of transactions.
                updateBalances(it)
                // Update the RecyclerView with only the 5 most recent transactions.
                transactionAdapter.updateTransactions(it.take(5))
            }
        })
    }

    private fun updateBalances(transactions: List<Transaction>) {
        if (transactions.isEmpty()) {
            binding.tvTotalBalance.text = "₹0.00"
            binding.tvIncome.text = "₹0.00"
            binding.tvExpense.text = "₹0.00"
        } else {
            val totalIncome = transactions.filter { it.type == "income" }.sumOf { it.amount }
            val totalExpense = transactions.filter { it.type == "expense" }.sumOf { it.amount }
            val balance = totalIncome - totalExpense

            binding.tvTotalBalance.text = "₹${String.format("%.2f", balance)}"
            binding.tvIncome.text = "₹${String.format("%.2f", totalIncome)}"
            binding.tvExpense.text = "₹${String.format("%.2f", totalExpense)}"
        }
    }

    private fun setupClickListeners() {
        binding.btnAddIncome.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply { putExtra("type", "income") }
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnAddExpense.setOnClickListener {
            val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply { putExtra("type", "expense") }
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnViewReports.setOnClickListener { (activity as? MainActivity)?.binding?.bottomNavigation?.selectedItemId = R.id.nav_reports }
        binding.btnBudget.setOnClickListener { (activity as? MainActivity)?.binding?.bottomNavigation?.selectedItemId = R.id.nav_budget }
        binding.tvSeeAll.setOnClickListener { (activity as? MainActivity)?.binding?.bottomNavigation?.selectedItemId = R.id.nav_transactions }

        binding.ivNotification.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                when {
                    ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED -> {
                        NotificationHelper.showSampleNotification(requireContext())
                    }
                    shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS) -> {
                        AlertDialog.Builder(requireContext())
                            .setTitle("Notification Permission Needed")
                            .setMessage("This app needs your permission to show future alerts and reminders.")
                            .setPositiveButton("OK") { _, _ -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) }
                            .create().show()
                    }
                    else -> requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                NotificationHelper.showSampleNotification(requireContext())
            }
        }
    }

    private fun openEditTransaction(transaction: Transaction) {
        val intent = Intent(requireContext(), AddTransactionActivity::class.java).apply {
            putExtra("edit_mode", true)
            putExtra("editing_transaction", transaction) // Pass the whole serializable object
        }
        startActivity(intent)
        requireActivity().overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}