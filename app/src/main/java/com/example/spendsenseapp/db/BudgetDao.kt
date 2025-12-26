package com.example.spendsense.db

import androidx.room.*
import com.example.spendsense.models.Budget
import kotlinx.coroutines.flow.Flow

// Mark this as a Data Access Object for Room
@Dao
interface BudgetDao {

    /**
     * Inserts a new budget. If a budget with the same ID already exists,
     * it will be replaced.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateBudget(budget: Budget)

    /**
     * Deletes a specific budget from the table.
     */
    @Delete
    suspend fun deleteBudget(budget: Budget)

    /**
     * Queries for all budgets for a specific month (e.g., "2024-01").
     * It returns a Flow, so the UI can observe it and update automatically
     * whenever the budgets for that month change.
     */
    @Query("SELECT * FROM budgets WHERE month = :month")
    fun getBudgetsForMonth(month: String): Flow<List<Budget>>

    /**
     * Deletes all budgets from the table. This will be used when the user
     * clears their data.
     */
    @Query("DELETE FROM budgets")
    suspend fun deleteAllBudgets()
}