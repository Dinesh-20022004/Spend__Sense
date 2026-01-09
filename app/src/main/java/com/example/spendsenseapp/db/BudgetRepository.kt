package com.example.spendsense.db

import androidx.annotation.WorkerThread
import com.example.spendsense.models.Budget
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val budgetDao: BudgetDao) {

    fun getBudgetsForMonth(month: String, email: String): Flow<List<Budget>> {
        return budgetDao.getBudgetsForMonth(month, email)
    }

    @WorkerThread
    suspend fun insertOrUpdateBudget(budget: Budget) = budgetDao.insertOrUpdateBudget(budget)

    @WorkerThread
    suspend fun deleteBudget(budget: Budget) = budgetDao.deleteBudget(budget)

    @WorkerThread
    suspend fun deleteAllBudgets(email: String) = budgetDao.deleteAllBudgets(email)
}