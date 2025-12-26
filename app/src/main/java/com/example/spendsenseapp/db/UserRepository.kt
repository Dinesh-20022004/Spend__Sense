package com.example.spendsense.db

import androidx.annotation.WorkerThread
import com.example.spendsense.models.User

/**
 * The repository for handling user data. It takes a UserDao as a parameter.
 * It provides a clean API for the ViewModel to interact with user data,
 * hiding the fact that the data is coming from a Room database.
 */
class UserRepository(private val userDao: UserDao) {

    /**
     * A suspending function to register a new user.
     * The @WorkerThread annotation is a good practice to indicate that this
     * function should be called from a background thread (which the ViewModel's coroutine will handle).
     * It simply calls the corresponding function in the DAO.
     */
    @WorkerThread
    suspend fun registerUser(user: User) {
        userDao.registerUser(user)
    }

    /**
     * A suspending function to retrieve a user by their email address.
     * This will be used during the login process.
     * It returns a nullable User object, as the user might not exist.
     */
    @WorkerThread
    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }
}