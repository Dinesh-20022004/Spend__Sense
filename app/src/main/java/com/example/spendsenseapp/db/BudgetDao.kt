package com.example.spendsense.db

import androidx.room.*
import com.example.spendsense.models.Budget
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)

    @Delete
    suspend fun deleteBudget(budget: Budget)

    // FILTER BY EMAIL AND MONTH
    @Query("SELECT * FROM budgets WHERE month = :month AND userEmail = :email")
    fun getBudgetsForMonth(month: String, email: String): Flow<List<Budget>>

    // DELETE ONLY THIS USER'S BUDGETS
    @Query("DELETE FROM budgets WHERE userEmail = :email")
    suspend fun deleteAllBudgets(email: String)
}