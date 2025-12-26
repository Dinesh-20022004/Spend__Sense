package com.example.spendsense

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.spendsense.databinding.ActivityMainBinding
import com.example.spendsense.fragments.*

class MainActivity : AppCompatActivity() {

    // Using lateinit for the binding, as it will be initialized in onCreate.
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inflate the layout and set the content view.
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // This check ensures that the initial fragment is loaded only once,
        // when the activity is first created, and not on configuration changes (like screen rotation).
        if (savedInstanceState == null) {
            loadFragment(HomeFragment())
        }

        // Set up the listeners for the UI components.
        setupBottomNavigation()
        setupFab()
    }

    private fun setupBottomNavigation() {
        // Set a listener that will be called when a bottom navigation item is selected.
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val fragment: Fragment = when (item.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_transactions -> TransactionsFragment()
                R.id.nav_reports -> ReportsFragment()
                R.id.nav_budget -> BudgetFragment()
                R.id.nav_profile -> ProfileFragment()
                else -> HomeFragment() // Default to HomeFragment if something goes wrong.
            }
            loadFragment(fragment)
            true // Return true to display the item as the selected item.
        }
    }

    private fun setupFab() {
        // Set a listener for the Floating Action Button.
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
            // Apply a custom animation for the screen transition.
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }
    }

    private fun loadFragment(fragment: Fragment) {
        // This is the standard way to replace a fragment in a container.
        // We use .replace() because only one fragment should be visible at a time with bottom navigation.
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}