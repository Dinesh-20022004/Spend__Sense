package com.example.spendsense.db

import androidx.annotation.WorkerThread
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    // Now requires email to get data
    fun getAllTransactions(email: String): Flow<List<Transaction>> {
        return transactionDao.getAllTransactions(email)
    }

    @WorkerThread
    suspend fun insert(transaction: Transaction) = transactionDao.insert(transaction)

    @WorkerThread
    suspend fun update(transaction: Transaction) = transactionDao.update(transaction)

    @WorkerThread
    suspend fun delete(transaction: Transaction) = transactionDao.delete(transaction)

    @WorkerThread
    suspend fun deleteAll(email: String) = transactionDao.deleteAllTransactions(email)
}