package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.UserSessionManager
import com.example.spendsense.db.AppDatabase
import com.example.spendsense.db.TransactionRepository
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository,
    private val userEmail: String
) : ViewModel() {

    // Pass email to repository to filter data
    val allTransactions: LiveData<List<Transaction>> = repository.getAllTransactions(userEmail).asLiveData()

    fun insert(transaction: Transaction) = viewModelScope.launch { repository.insert(transaction) }
    fun update(transaction: Transaction) = viewModelScope.launch { repository.update(transaction) }
    fun delete(transaction: Transaction) = viewModelScope.launch { repository.delete(transaction) }
    fun deleteAll() = viewModelScope.launch { repository.deleteAll(userEmail) }
}

class TransactionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            // 1. Get the database instance
            val database = AppDatabase.getDatabase(application)

            // 2. Create the repository using the DAO from the database
            val repository = TransactionRepository(database.transactionDao())

            // 3. Get the current user's email
            val email = UserSessionManager.getLoggedInEmail(application) ?: ""

            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository, email) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}