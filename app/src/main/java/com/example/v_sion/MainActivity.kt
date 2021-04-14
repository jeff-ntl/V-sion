package com.example.v_sion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.viewpager.widget.ViewPager
import com.example.v_sion.fragments.FocusLockFragment
import com.example.v_sion.fragments.HomeFragment
import com.example.v_sion.fragments.TargetAppFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*

//class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val host: NavHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment? ?: return

        // Set up Action Bar
        val navController = host.navController

        setupBottomNavMenu(navController)
    }

    fun setupBottomNavMenu(navController: NavController) {
        bottom_nav_view.setupWithNavController(navController)
    }
}