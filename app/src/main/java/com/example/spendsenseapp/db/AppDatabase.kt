package com.example.spendsense.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendsense.models.Budget
import com.example.spendsense.models.Transaction
import com.example.spendsense.models.User

@Database(entities = [Transaction::class, User::class, Budget::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "spendsense_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}