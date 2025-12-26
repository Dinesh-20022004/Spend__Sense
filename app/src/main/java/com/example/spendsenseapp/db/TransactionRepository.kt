package com.example.spendsense.db

import androidx.annotation.WorkerThread
import com.example.spendsense.models.Transaction
import kotlinx.coroutines.flow.Flow

class TransactionRepository(private val transactionDao: TransactionDao) {

    // This 'Flow' will emit a new list of transactions whenever the database changes.
    // The ViewModel will observe this flow.
    val allTransactions: Flow<List<Transaction>> = transactionDao.getAllTransactions()

    // The 'suspend' modifier tells the compiler that this needs to be called
    // from a coroutine or another suspending function. Room handles running
    // these on a background thread for you.
    @WorkerThread
    suspend fun insert(transaction: Transaction) {
        transactionDao.insert(transaction)
    }

    @WorkerThread
    suspend fun update(transaction: Transaction) {
        transactionDao.update(transaction)
    }

    @WorkerThread
    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }

    @WorkerThread
    suspend fun deleteAll() {
        transactionDao.deleteAllTransactions()
    }
}