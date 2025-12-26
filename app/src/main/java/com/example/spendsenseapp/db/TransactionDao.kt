package com.example.spendsense.db

import androidx.room.*
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {

    // --- Insert Operation ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    // --- Update Operation ---
    @Update
    suspend fun update(transaction: Transaction)

    // --- Delete Operation ---
    @Delete
    suspend fun delete(transaction: Transaction)

    // --- Query to get all transactions ---
    // It returns a Flow, which is a stream of data that automatically
    // updates the UI whenever the data in the table changes.
    @Query("SELECT * FROM transactions ORDER BY date DESC, id DESC")
    fun getAllTransactions(): Flow<List<Transaction>>

    // --- Query to delete all transactions ---
    @Query("DELETE FROM transactions")
    suspend fun deleteAllTransactions()
}