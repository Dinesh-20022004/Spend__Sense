package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.SpendSenseApplication
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

// --- THIS IS THE CORRECTED FACTORY ---
class BudgetViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BudgetViewModel::class.java)) {
            // Get the DAO from the database, then create the repository inside the factory.
            val dao = (application as SpendSenseApplication).database.budgetDao()
            val repository = BudgetRepository(dao)

            @Suppress("UNCHECKED_CAST")
            return BudgetViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}