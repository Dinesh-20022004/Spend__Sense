package com.example.spendsense.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.spendsense.models.Budget
import com.example.spendsense.models.Transaction
import com.example.spendsense.models.User

// This annotation defines the schema of the database.
// version = 2 because we have added the User and Budget entities.
@Database(entities = [Transaction::class, User::class, Budget::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    // Abstract functions for each DAO.
    abstract fun transactionDao(): TransactionDao
    abstract fun userDao(): UserDao
    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Returns an instance of the database.
         * Crucially, it ensures that the database instance returned matches the 'userEmail' requested.
         * If the currently open database is for a different user (or is the login DB), it closes it
         * and opens the correct one.
         */
        fun getDatabase(context: Context, userEmail: String): AppDatabase {
            // Sanitize the email to make a valid filename
            val safeEmail = userEmail.replace("[@.]".toRegex(), "_")
            val targetDbName = "spendsense_db_$safeEmail"

            val currentInstance = INSTANCE

            // Check if an instance already exists
            if (currentInstance != null && currentInstance.isOpen) {
                // CRITICAL CHECK: Is the open database the one we actually want?
                if (currentInstance.openHelper.databaseName == targetDbName) {
                    // Yes, it is. Return it.
                    return currentInstance
                } else {
                    // No, it's a different database (e.g., we just switched users).
                    // Close the old one so we can create the new one.
                    currentInstance.close()
                    INSTANCE = null
                }
            }

            // Create the new database instance
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    targetDbName
                )
                    // This strategy deletes the old DB if the version changes. Good for development.
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                return instance
            }
        }

        /**
         * Explicitly closes the database connection.
         * Should be called during logout.
         */
        fun closeDatabase() {
            if (INSTANCE != null && INSTANCE!!.isOpen) {
                INSTANCE!!.close()
            }
            INSTANCE = null
        }
    }
}