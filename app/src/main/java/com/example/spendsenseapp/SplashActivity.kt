package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.spendsense.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Navigate after 2.5 seconds
        // In SplashActivity.kt, inside onCreate()

        Handler(Looper.getMainLooper()).postDelayed({
            // Check if there is a last logged-in user
            val loggedInEmail = UserSessionManager.getLoggedInEmail(this)

            val intent = if (loggedInEmail != null) {
                // A user is logged in, go to MainActivity
                Intent(this, MainActivity::class.java)
            } else {
                // No user is logged in, go to LoginActivity
                Intent(this, LoginActivity::class.java)
            }

            startActivity(intent)
            finish()
        }, 2500)
    }
}