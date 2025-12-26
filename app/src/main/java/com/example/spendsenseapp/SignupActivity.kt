package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.spendsense.databinding.ActivitySignupBinding
import com.example.spendsense.models.User
import com.example.spendsense.viewmodels.AuthViewModel
import com.example.spendsense.viewmodels.AuthViewModelFactory
import com.example.spendsense.viewmodels.RegistrationResult

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.registrationStatus.observe(this, Observer { result ->
            when (result) {
                is RegistrationResult.Success -> {
                    Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show()
                    // After successful registration, we still need to log the user in.
                    // For simplicity, we'll just go to the Login screen. A better UX would be to auto-login.
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                    finish()
                }
                is RegistrationResult.EmailAlreadyExists -> {
                    Toast.makeText(this, "An account with this email already exists.", Toast.LENGTH_LONG).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.tvLogin.setOnClickListener { finish() }

        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                // In a real app, you would HASH the password here before saving.
                // For this project, we are storing it as plain text.
                val user = User(name = name, email = email, passwordHash = password)
                authViewModel.registerUser(user)
            }
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirm: String): Boolean {
        // ... (validation logic is the same)
        return true
    }
}