package com.example.spendsense.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendsense.models.Budget
import com.example.spendsense.models.Transaction
import com.example.spendsense.models.User

// VERSION IS NOW 3
@Database(entities = [Transaction::class, User::class, Budget::class], version = 3, exportSchema = false)
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
                    // This will delete the old database (v2) and create a new empty one (v3)
                    // This is necessary because we changed the table structure.
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}