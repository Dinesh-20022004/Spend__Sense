package com.example.spendsense.fragments

import android.Manifest
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.spendsense.LoginActivity
import com.example.spendsense.R
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.UserSessionManager
import com.example.spendsense.databinding.FragmentProfileBinding
import com.example.spendsense.viewmodels.AuthViewModel
import com.example.spendsense.viewmodels.AuthViewModelFactory
import com.example.spendsense.viewmodels.BudgetViewModel
import com.example.spendsense.viewmodels.BudgetViewModelFactory
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(requireActivity().application)
    }
    private val budgetViewModel: BudgetViewModel by viewModels {
        BudgetViewModelFactory(requireActivity().application)
    }
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(requireActivity().application)
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                exportTransactionsToCSV()
            } else {
                Toast.makeText(requireContext(), "Permission denied. Cannot export data.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadUserInfo()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val userAccountsPrefs = requireActivity().getSharedPreferences("UserAccounts", Context.MODE_PRIVATE)
        val loggedInEmail = UserSessionManager.getLoggedInEmail(requireContext())

        if (loggedInEmail != null) {
            val storedData = userAccountsPrefs.getString(loggedInEmail, "User|")
            val name = storedData?.split("|")?.get(0) ?: "User"
            binding.tvUserName.text = name
            binding.tvUserEmail.text = loggedInEmail
        }
    }

    private fun setupClickListeners() {
        binding.btnEditProfile.setOnClickListener { showEditProfileDialog() }
        binding.btnCurrency.setOnClickListener { showCurrencyDialog() }
        binding.btnExportData.setOnClickListener { checkPermissionAndExport() }
        binding.btnClearData.setOnClickListener { showClearDataDialog() }
        binding.btnLogout.setOnClickListener { showLogoutDialog() }
    }

    private fun checkPermissionAndExport() {
        // ... (This function remains unchanged)
    }

    private fun exportTransactionsToCSV() {
        // ... (This function remains unchanged)
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileUsingMediaStore(csvData: String) {
        // ... (This function remains unchanged)
    }

    @Suppress("DEPRECATION")
    private fun saveFileUsingLegacyStorage(csvData: String) {
        // ... (This function remains unchanged)
    }

    private fun showEditProfileDialog() {
        // ... (This function remains unchanged)
    }

    private fun showCurrencyDialog() {
        // ... (This function remains unchanged)
    }

    private fun showClearDataDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Clear All Data")
            .setMessage("Are you sure you want to delete all transactions and budgets for this account?")
            .setPositiveButton("Clear All") { _, _ ->
                transactionViewModel.deleteAll()
                budgetViewModel.deleteAll()
                Toast.makeText(requireContext(), "All account data has been cleared!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // --- THIS IS THE CORRECTED FUNCTION ---
    private fun performLogout() {
        // AppDatabase.closeDatabase() // This line is removed.
        UserSessionManager.clearSession(requireContext())
        Toast.makeText(requireContext(), "Logged out successfully!", Toast.LENGTH_SHORT).show()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}