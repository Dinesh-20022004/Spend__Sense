package com.example.spendsense.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.example.spendsense.SpendSenseApplication
import com.example.spendsense.db.AppDatabase
import com.example.spendsense.db.UserRepository
import com.example.spendsense.models.User
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _registrationStatus = MutableLiveData<RegistrationResult>()
    val registrationStatus: LiveData<RegistrationResult> = _registrationStatus

    private val _loginStatus = MutableLiveData<LoginResult>()
    val loginStatus: LiveData<LoginResult> = _loginStatus

    fun registerUser(user: User) = viewModelScope.launch {
        val existingUser = repository.getUserByEmail(user.email)
        if (existingUser != null) {
            _registrationStatus.postValue(RegistrationResult.EmailAlreadyExists)
        } else {
            repository.registerUser(user)
            _registrationStatus.postValue(RegistrationResult.Success)
        }
    }

    fun loginUser(email: String, passwordAttempt: String) = viewModelScope.launch {
        val user = repository.getUserByEmail(email)
        if (user != null && user.passwordHash == passwordAttempt) {
            _loginStatus.postValue(LoginResult.Success(user))
        } else {
            _loginStatus.postValue(LoginResult.InvalidCredentials)
        }
    }
}

sealed class RegistrationResult {
    object Success : RegistrationResult()
    object EmailAlreadyExists : RegistrationResult()
}

sealed class LoginResult {
    data class Success(val user: User) : LoginResult()
    object InvalidCredentials : LoginResult()
}

// --- THIS IS THE FIXED FACTORY ---
class AuthViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {

            // FIX: Call getDatabase with ONLY the application context.
            // We removed the second argument ("secure_user_store") from AppDatabase.
            val database = AppDatabase.getDatabase(application)

            val dao = database.userDao()
            val repository = UserRepository(dao)

            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}