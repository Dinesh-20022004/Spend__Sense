package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.UserSessionManager
import com.example.spendsense.db.AppDatabase
import com.example.spendsense.db.BudgetRepository
import com.example.spendsense.models.Budget
import kotlinx.coroutines.launch

class BudgetViewModel(
    private val repository: BudgetRepository,
    private val userEmail: String
) : ViewModel() {

    fun getBudgetsForMonth(month: String): LiveData<List<Budget>> {
        return repository.getBudgetsForMonth(month, userEmail).asLiveData()
    }

    fun insert(budget: Budget) = viewModelScope.launch {
        repository.insertOrUpdateBudget(budget)
    }

    fun delete(budget: Budget) = viewModelScope.launch {
        repository.deleteBudget(budget)
    }

    fun deleteAll() = viewModelScope.launch {
        repository.deleteAllBudgets(userEmail)
    }
}

class BudgetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            // 1. Get the database instance
            val database = AppDatabase.getDatabase(application)

            // 2. Create the repository using the DAO from the database
            val repository = BudgetRepository(database.budgetDao())

            // 3. Get the current user's email
            val email = UserSessionManager.getLoggedInEmail(application) ?: ""

            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository, email) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}