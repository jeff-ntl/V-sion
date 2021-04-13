package com.example.v_sion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.example.v_sion.adapters.ViewPagerAdapter
import com.example.v_sion.fragments.FocusLockFragment
import com.example.v_sion.fragments.HomeFragment
import com.example.v_sion.fragments.TargetAppFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.content_main.*

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //navigation drawer
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.open,R.string.close)
        toggle.isDrawerIndicatorEnabled = true
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setHomeButtonEnabled(true)

        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        //bottom navigation
        setUpTabs()
    }

    private fun setUpTabs(){
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val tabs: TabLayout = findViewById(R.id.tabs)

        val adapter = ViewPagerAdapter(supportFragmentManager)
        adapter.addFragment(TargetAppFragment(), "TargetApp")
        adapter.addFragment(HomeFragment(), "Home")
        adapter.addFragment(FocusLockFragment(), "FocusLock")
        viewPager.adapter = adapter

        tabs.setupWithViewPager(viewPager)

        tabs.getTabAt(0)!!.setIcon(R.drawable.ic_baseline_filter_center_focus_24)
        tabs.getTabAt(1)!!.setIcon(R.drawable.ic_baseline_home_24)
        tabs.getTabAt(2)!!.setIcon(R.drawable.ic_baseline_lock_24)

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_item_profile -> Toast.makeText(this, "Clicked item one", Toast.LENGTH_SHORT).show()
            R.id.nav_item_record -> Toast.makeText(this, "Clicked item two", Toast.LENGTH_SHORT).show()
            R.id.nav_item_leaderboard -> Toast.makeText(this, "Clicked item three", Toast.LENGTH_SHORT).show()
            R.id.nav_item_logout -> Toast.makeText(this, "Clicked item four", Toast.LENGTH_SHORT).show()
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    //handle back button clicked action
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}