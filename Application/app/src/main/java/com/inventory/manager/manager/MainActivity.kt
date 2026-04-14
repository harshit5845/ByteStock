package com.inventory.manager

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.inventory.manager.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // All bottom nav tabs are top-level — no back stack buildup between them
        val topLevelDestinations = setOf(
            R.id.homeFragment,
            R.id.inventoryFragment,
            R.id.billingFragment,
            R.id.analyticsFragment,
            R.id.profileFragment,
            R.id.chatbotFragment
        )
        AppBarConfiguration(topLevelDestinations)

        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.setOnItemReselectedListener { /* stay put */ }

        // Each tab tap clears back stack properly
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, false, true)
                .build()
            try { navController.navigate(item.itemId, null, navOptions); true }
            catch (e: Exception) { false }
        }

        // ── Floating Chat Button ──────────────────────────────────────────────
        binding.fabChat.setOnClickListener {
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setRestoreState(true)
                .setPopUpTo(navController.graph.startDestinationId, false, true)
                .build()
            navController.navigate(R.id.chatbotFragment, null, navOptions)
        }

        // Show/hide FAB and bottom nav based on current destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.chatbotFragment -> {
                    // Hide FAB when already on chatbot
                    binding.fabChat.hide()
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
                else -> {
                    // Show pulsing chat FAB on all other screens
                    binding.fabChat.show()
                    binding.bottomNavigationView.visibility = View.VISIBLE
                }
            }
        }
    }

    override fun onBackPressed() {
        if (navController.currentDestination?.id != R.id.homeFragment) {
            navController.navigate(
                R.id.homeFragment, null,
                androidx.navigation.NavOptions.Builder()
                    .setLaunchSingleTop(true)
                    .setPopUpTo(R.id.homeFragment, false)
                    .build()
            )
        } else {
            super.onBackPressed()
        }
    }
}
