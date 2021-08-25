package com.example.v_sion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.*
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info

class MainActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        // Finding the Navigation Controller
        var navController = findNavController(R.id.navHostFragment)

        // Setting Up ActionBar with Navigation Controller
        // Pass the IDs of top-level destinations in AppBarConfiguration
        appBarConfiguration = AppBarConfiguration(
            topLevelDestinationIds = setOf(
                R.id.targetAppFragment,
                R.id.homeFragment,
                R.id.focusLockFragment
            )
        )
        // Bottom Navigation
        setupBottomNavMenu(navController)
        setupActionBar(navController)


    }

    private fun setupBottomNavMenu(navController: NavController) {
        // change toolbar label (Target App, Home, Focus Lock)
        bottom_nav_view.setupWithNavController(navController)
    }

    private fun setupActionBar(navController: NavController) {
        // Display the Up button whenever you're not on a top-level destination
        setupActionBarWithNavController(navController, appBarConfiguration)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.overflow_menu, menu)
        return true
    }

    // Handle navigation with onNavDestinationSelected helper method,
    // if the menu item is not meant to navigate, handel with super.onOptionsItemSelected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return item.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                || super.onOptionsItemSelected(item)
    }

    // Handle up navigation behavior in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp(appBarConfiguration)
    }


}