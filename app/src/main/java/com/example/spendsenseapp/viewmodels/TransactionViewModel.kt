package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.UserSessionManager
import com.example.spendsense.db.AppDatabase
import com.example.spendsense.db.TransactionRepository
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.launch

class TransactionViewModel(private val repository: TransactionRepository) : ViewModel() {

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions.asLiveData()

    fun insert(transaction: Transaction) = viewModelScope.launch {
        repository.insert(transaction)
    }

    fun update(transaction: Transaction) = viewModelScope.launch {
        repository.update(transaction)
    }

    fun delete(transaction: Transaction) = viewModelScope.launch {
        repository.delete(transaction)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAll()
    }
}

/**
 * Factory for creating a TransactionViewModel with a dependency on the repository.
 */
class TransactionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            // 1. Get the current user's email
            val userEmail = UserSessionManager.getLoggedInEmail(application)

            if (userEmail != null) {
                // 2. Get the specific database for THIS user
                val database = AppDatabase.getDatabase(application, userEmail)

                // 3. Create the repository with the correct DAO
                val repository = TransactionRepository(database.transactionDao())

                @Suppress("UNCHECKED_CAST")
                return TransactionViewModel(repository) as T
            }
            throw IllegalArgumentException("User not logged in, cannot create ViewModel")
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}