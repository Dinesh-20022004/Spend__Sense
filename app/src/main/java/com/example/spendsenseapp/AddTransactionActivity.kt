package com.example.spendsense

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.spendsense.databinding.ActivityAddTransactionBinding
import com.example.spendsense.models.Transaction
import com.example.spendsense.viewmodels.TransactionViewModel
import com.example.spendsense.viewmodels.TransactionViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddTransactionBinding
    private var selectedType = "expense"
    private var selectedDate = Calendar.getInstance()
    private var isEditMode = false
    private var editingTransactionId: Int = 0

    // THIS IS THE CORRECTED INITIALIZATION FOR AN ACTIVITY
    // NEW AND CORRECT
    private val transactionViewModel: TransactionViewModel by viewModels {
        TransactionViewModelFactory(application)
    }

    private val expenseCategories = listOf("Food", "Transport", "Shopping", "Bills", "Entertainment", "Health", "Education", "Other")
    private val incomeCategories = listOf("Salary", "Freelance", "Business", "Investment", "Gift", "Other")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        val editingTransaction = intent.getSerializableExtra("editing_transaction") as? Transaction
        isEditMode = (editingTransaction != null)

        if (isEditMode && editingTransaction != null) {
            loadTransactionForEdit(editingTransaction)
        } else {
            val typeFromIntent = intent.getStringExtra("type")
            if (typeFromIntent != null) {
                selectedType = typeFromIntent
            }
            updateTypeButtons()
            updateDateDisplay()
        }

        setupClickListeners()
        setupCategoryDropdown()
        setupDatePicker()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnExpense.setOnClickListener {
            selectedType = "expense"
            updateTypeButtons()
            setupCategoryDropdown()
        }

        binding.btnIncome.setOnClickListener {
            selectedType = "income"
            updateTypeButtons()
            setupCategoryDropdown()
        }

        binding.btnSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateTypeButtons() {
        if (selectedType == "expense") {
            binding.btnExpense.setBackgroundColor(ContextCompat.getColor(this, R.color.expense_red))
            binding.btnExpense.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnIncome.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            binding.btnIncome.setTextColor(ContextCompat.getColor(this, R.color.income_green))
        } else {
            binding.btnIncome.setBackgroundColor(ContextCompat.getColor(this, R.color.income_green))
            binding.btnIncome.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.btnExpense.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent))
            binding.btnExpense.setTextColor(ContextCompat.getColor(this, R.color.expense_red))
        }
    }

    private fun setupCategoryDropdown() {
        val categories = if (selectedType == "expense") expenseCategories else incomeCategories
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val datePickerDialog = DatePickerDialog(
                this,
                { _, year, month, dayOfMonth ->
                    selectedDate.set(year, month, dayOfMonth)
                    updateDateDisplay()
                },
                selectedDate.get(Calendar.YEAR),
                selectedDate.get(Calendar.MONTH),
                selectedDate.get(Calendar.DAY_OF_MONTH)
            )
            datePickerDialog.show()
        }
    }

    private fun updateDateDisplay() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.etDate.setText(dateFormat.format(selectedDate.time))
    }

    private fun loadTransactionForEdit(transaction: Transaction) {
        binding.tvTitle.text = "Edit Transaction"
        editingTransactionId = transaction.id
        selectedType = transaction.type
        updateTypeButtons()
        binding.etAmount.setText(transaction.amount.toString())
        binding.etTitle.setText(transaction.title)
        binding.etNote.setText(transaction.note)

        try {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(transaction.date)
            if (date != null) selectedDate.time = date
        } catch (e: Exception) { e.printStackTrace() }
        updateDateDisplay()

        binding.actvCategory.post { binding.actvCategory.setText(transaction.category, false) }
    }

    private fun saveTransaction() {
        val amountText = binding.etAmount.text.toString().trim()
        val title = binding.etTitle.text.toString().trim()
        val category = binding.actvCategory.text.toString().trim()
        val note = binding.etNote.text.toString().trim()

        if (!validateInputs(amountText, title, category)) return

        val amount = amountText.toDouble()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val transactionToSave = Transaction(
            id = if (isEditMode) editingTransactionId else 0,
            title = title, amount = amount, type = selectedType,
            category = category, date = dateFormat.format(selectedDate.time), note = note
        )

        if (isEditMode) {
            transactionViewModel.update(transactionToSave)
            Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show()
        } else {
            transactionViewModel.insert(transactionToSave)
            Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show()
        }

        finish()
    }

    private fun validateInputs(amount: String, title: String, category: String): Boolean {
        if (amount.isEmpty() || amount.toDoubleOrNull() == null || amount.toDouble() <= 0) {
            binding.tilAmount.error = "Please enter a valid amount"
            return false
        } else {
            binding.tilAmount.error = null
        }

        if (title.isEmpty()) {
            binding.tilTitle.error = "Title is required"
            return false
        } else {
            binding.tilTitle.error = null
        }

        if (category.isEmpty()) {
            binding.tilCategory.error = "Category is required"
            return false
        } else {
            binding.tilCategory.error = null
        }

        return true
    }
}