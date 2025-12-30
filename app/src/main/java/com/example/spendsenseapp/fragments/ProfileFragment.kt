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
    // AuthViewModel not strictly needed here but fine to keep
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            exportTransactionsToCSV()
            return
        }

        when {
            ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                exportTransactionsToCSV()
            }
            shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) -> {
                AlertDialog.Builder(requireContext())
                    .setTitle("Storage Permission Needed")
                    .setMessage("This app needs permission to save the CSV file.")
                    .setPositiveButton("OK") { _, _ ->
                        requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    }
                    .create()
                    .show()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }
    }

    private fun exportTransactionsToCSV() {
        val transactions = transactionViewModel.allTransactions.value ?: emptyList()

        if (transactions.isEmpty()) {
            Toast.makeText(requireContext(), "No transactions to export.", Toast.LENGTH_SHORT).show()
            return
        }

        val csvHeader = "ID,Title,Amount,Type,Category,Date,Note\n"
        val stringBuilder = StringBuilder().append(csvHeader)

        transactions.forEach { t ->
            val title = if (t.title.contains(",")) "\"${t.title}\"" else t.title
            val note = if (t.note.contains(",")) "\"${t.note}\"" else t.note
            stringBuilder.append("${t.id},$title,${t.amount},${t.type},${t.category},${t.date},$note\n")
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveFileUsingMediaStore(stringBuilder.toString())
            } else {
                saveFileUsingLegacyStorage(stringBuilder.toString())
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Export failed: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    @androidx.annotation.RequiresApi(Build.VERSION_CODES.Q)
    private fun saveFileUsingMediaStore(csvData: String) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "SpendSense_Export_${System.currentTimeMillis()}.csv")
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }
        val resolver = requireContext().contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        uri?.let {
            resolver.openOutputStream(it).use { outputStream ->
                outputStream?.write(csvData.toByteArray())
            }
            Toast.makeText(requireContext(), "Export successful! Saved to Downloads.", Toast.LENGTH_LONG).show()
        } ?: throw Exception("MediaStore failed to create file.")
    }

    @Suppress("DEPRECATION")
    private fun saveFileUsingLegacyStorage(csvData: String) {
        val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "SpendSense_Export_${System.currentTimeMillis()}.csv")
        FileWriter(file).use { it.write(csvData) }
        Toast.makeText(requireContext(), "Export successful! Saved to Downloads.", Toast.LENGTH_LONG).show()
    }

    private fun showEditProfileDialog() {
        val userAccountsPrefs = requireActivity().getSharedPreferences("UserAccounts", Context.MODE_PRIVATE)
        val loggedInEmail = UserSessionManager.getLoggedInEmail(requireContext()) ?: return
        val storedData = userAccountsPrefs.getString(loggedInEmail, "") ?: ""
        val currentName = storedData.split("|").getOrNull(0) ?: ""

        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_edit_profile, null)
        val etName = dialogView.findViewById<EditText>(R.id.etName)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)

        etName.setText(currentName)
        etEmail.setText(loggedInEmail)
        etEmail.isEnabled = false

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Profile")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = etName.text.toString().trim()
                if (newName.isNotEmpty()) {
                    val password = storedData.split("|").getOrNull(1) ?: ""
                    userAccountsPrefs.edit().putString(loggedInEmail, "$newName|$password").apply()
                    loadUserInfo()
                    Toast.makeText(requireContext(), "Profile updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Name cannot be empty.", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showCurrencyDialog() {
        val currencies = arrayOf("₹ Indian Rupee (INR)", "$ US Dollar (USD)", "€ Euro (EUR)")
        AlertDialog.Builder(requireContext())
            .setTitle("Select Currency")
            .setItems(currencies) { _, which ->
                binding.tvCurrency.text = currencies[which]
                Toast.makeText(requireContext(), "Currency setting coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
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

    private fun performLogout() {
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