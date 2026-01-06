package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.UserSessionManager
import com.example.spendsense.db.AppDatabase
import com.example.spendsense.db.BudgetRepository
import com.example.spendsense.models.Budget
import kotlinx.coroutines.launch

class BudgetViewModel(private val repository: BudgetRepository) : ViewModel() {

    fun getBudgetsForMonth(month: String): LiveData<List<Budget>> {
        return repository.getBudgetsForMonth(month).asLiveData()
    }

    fun insert(budget: Budget) = viewModelScope.launch {
        repository.insertOrUpdateBudget(budget)
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllBudgets()
    }
}

// FACTORY
class BudgetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            // Get the current user's email to access their specific database
            val userEmail = UserSessionManager.getLoggedInEmail(application)

            if (userEmail != null) {
                // Get the user-specific database
                val database = AppDatabase.getDatabase(application, userEmail)
                val dao = database.budgetDao()
                val repository = BudgetRepository(dao)

                @Suppress("UNCHECKED_CAST")
                return BudgetViewModel(repository) as T
            }
            // If no user is logged in (shouldn't happen here), handle safely
            throw IllegalArgumentException("User not logged in, cannot create BudgetViewModel")
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}