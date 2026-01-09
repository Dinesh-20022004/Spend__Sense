package com.example.spendsense.db

import androidx.room.*
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    // FILTER BY EMAIL
    @Query("SELECT * FROM transactions WHERE userEmail = :email ORDER BY date DESC, id DESC")
    fun getAllTransactions(email: String): Flow<List<Transaction>>

    // DELETE ONLY THIS USER'S DATA
    @Query("DELETE FROM transactions WHERE userEmail = :email")
    suspend fun deleteAllTransactions(email: String)
}