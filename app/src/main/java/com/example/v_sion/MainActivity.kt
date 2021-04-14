package com.example.v_sion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Bottom Navigation
        setupBottomNavMenu()
    }

    private fun setupBottomNavMenu() {
        // Finding the Navigation Controller
        var navController = findNavController(R.id.navHostFragment)

        bottom_nav_view.setupWithNavController(navController)

        // Setting Up ActionBar with Navigation Controller
        // Pass the IDs of top-level destinations in AppBarConfiguration
        var appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf (
                R.id.targetAppFragment,
                R.id.homeFragment,
                R.id.focusLockFragment
            )
        )
        // change toolbar label
        setupActionBarWithNavController(navController, appBarConfiguration)
    }


}