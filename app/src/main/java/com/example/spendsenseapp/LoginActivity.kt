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
                    // Login successful! Save the user's email to the session.
                    UserSessionManager.setLoggedInEmail(this, result.user.email)

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
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                authViewModel.loginUser(email, password)
            }
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        // ... (validation logic is the same)
        return true
    }
}