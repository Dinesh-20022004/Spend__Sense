package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.db.TransactionRepository
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {
    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()
    fun insert(transaction: Transaction) = viewModelScope.launch { repository.insert(transaction) }
    fun update(transaction: Transaction) = viewModelScope.launch { repository.update(transaction) }
    fun delete(transaction: Transaction) = viewModelScope.launch { repository.delete(transaction) }
    fun deleteAll() = viewModelScope.launch { repository.deleteAll() }
}

/**
 * FINAL, CORRECT ViewModelFactory.
 * It takes the Application context, gets the database from it,
 * and then creates the repository itself.
 */
class TransactionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            // Get the DAO from the database instance provided by the Application class
            val dao = (application as SpendSenseApplication).database.transactionDao()
            // Create the repository right here
            val repository = TransactionRepository(dao)

            @Suppress("UNCHECKED_CAST")
            return TransactionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}