package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.spendsense.databinding.ActivityLoginBinding
import com.example.spendsense.viewmodels.AuthViewModel
import com.example.spendsense.viewmodels.AuthViewModelFactory
import com.example.spendsense.viewmodels.LoginResult

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    // Use the AuthViewModel to check credentials against the Database
    private val authViewModel: AuthViewModel by viewModels {
        AuthViewModelFactory(application)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeViewModel()
    }

    private fun observeViewModel() {
        authViewModel.loginStatus.observe(this, Observer { result ->
            when (result) {
                is LoginResult.Success -> {
                    // 1. Save the email so we know WHO is logged in (for database isolation)
                    UserSessionManager.setLoggedInEmail(this, result.user.email)

                    // 2. Save the NAME so we can display it on Home/Profile screens (THE FIX)
                    UserSessionManager.saveUserName(this, result.user.name)

                    Toast.makeText(this, "Welcome back, ${result.user.name}!", Toast.LENGTH_SHORT).show()

                    // 3. Navigate to Home
                    val intent = Intent(this, MainActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
                is LoginResult.InvalidCredentials -> {
                    Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun setupClickListeners() {
        binding.tvSignup.setOnClickListener {
            startActivity(Intent(this, SignupActivity::class.java))
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                // Ask ViewModel to check DB
                authViewModel.loginUser(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            binding.tilEmail.error = "Email is required"
            return false
        }
        if (password.isEmpty()) {
            binding.tilPassword.error = "Password is required"
            return false
        }
        return true
    }
}