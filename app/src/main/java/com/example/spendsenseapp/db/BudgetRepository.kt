package com.example.spendsense.db

import androidx.annotation.WorkerThread
import com.example.spendsense.models.Budget
import kotlinx.coroutines.flow.Flow

/**
 * The repository for handling budget data. It abstracts the data source (BudgetDao).
 */
class BudgetRepository(private val budgetDao: BudgetDao) {

    /**
     * A function that returns a Flow of budgets for a given month.
     * The ViewModel will observe this Flow to get real-time updates.
     *
     * @param month The month to query for, in "yyyy-MM" format.
     */
    fun getBudgetsForMonth(month: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(month)
    }

    /**
     * A suspending function to insert or update a budget.
     */
    @WorkerThread
    suspend fun insertOrUpdateBudget(budget: Budget) {
        budgetDao.insertOrUpdateBudget(budget)
    }

    /**
     * A suspending function to delete a specific budget.
     */
    @WorkerThread
    suspend fun deleteBudget(budget: Budget) {
        budgetDao.deleteBudget(budget)
    }

    /**
     * A suspending function to delete all budgets.
     */
    @WorkerThread
    suspend fun deleteAllBudgets() {
        budgetDao.deleteAllBudgets()
    }
}