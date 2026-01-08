package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendsense.databinding.ActivityMainBinding
import com.example.spendsense.fragments.*

class MainActivity : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()
        setupFab()

        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
            showFab(true) // Ensure FAB is visible on Home by default
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> {
                    showFab(true) // Show
                    HomeFragment()
                }
                R.id.nav_transactions -> {
                    showFab(true) // Show
                    TransactionsFragment()
                }
                R.id.nav_reports -> {
                    showFab(false) // Hide
                    ReportsFragment()
                }
                R.id.nav_budget -> {
                    showFab(false) // Hide
                    BudgetFragment()
                }
                R.id.nav_profile -> {
                    showFab(false) // Hide
                    ProfileFragment()
                }
                else -> {
                    showFab(true)
                    HomeFragment()
                }
            }
            loadFragment(fragment)
            true
        }
    }

    private fun showFab(show: Boolean) {
        if (show) {
            binding.fabAddTransaction.show()
        } else {
            binding.fabAddTransaction.hide()
        }
    }

    private fun setupFab() {
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}