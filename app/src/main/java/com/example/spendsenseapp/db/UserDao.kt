package com.example.spendsense.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.spendsense.models.User

// Mark this interface as a Data Access Object for Room
@Dao
interface UserDao {

    /**
     * Inserts a new user into the 'users' table.
     * The 'suspend' keyword means this function must be called from a coroutine (background thread).
     * OnConflictStrategy.IGNORE means if we try to insert a user with an email that already exists,
     * the operation will be ignored, preventing duplicates.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun registerUser(user: User)

    /**
     * Selects a user from the 'users' table based on their email.
     * The ':email' in the query is a placeholder that will be replaced by the
     * 'email' parameter passed to the function.
     * This will be used during login to check if a user exists.
     * It returns a nullable User? because the user might not be found.
     */
    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?
}