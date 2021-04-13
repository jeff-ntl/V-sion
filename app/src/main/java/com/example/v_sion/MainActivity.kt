package com.example.v_sion

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.viewpager.widget.ViewPager
import com.example.v_sion.adapters.ViewPagerAdapter
import com.example.v_sion.fragments.FocusLockFragment
import com.example.v_sion.fragments.HomeFragment
import com.example.v_sion.fragments.TargetAppFragment
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

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
}