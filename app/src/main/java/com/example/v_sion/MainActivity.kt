package com.example.v_sion

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.*
import com.example.v_sion.fragments.HomeFragment
import com.example.v_sion.main.MainApp
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.android.synthetic.main.dialog_timer.view.*
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.info
import org.jetbrains.anko.startActivity

class MainActivity : AppCompatActivity(), AnkoLogger {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var homeFragment: HomeFragment

    lateinit var app: MainApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        app = application as MainApp

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
    // if the menu item is not meant to navigate, handle with super.onOptionsItemSelected
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId) {
            R.id.item_timer -> {
                //Inflate the dialog with custom view
                val mDialogView = LayoutInflater.from(this).inflate(R.layout.dialog_timer, null)
                //AlertDialogBuilder
                val mBuilder = AlertDialog.Builder(this)
                    .setView(mDialogView)
                    .setTitle("Set your target time")
                //show dialog
                val  mAlertDialog = mBuilder.show()

                //set range of time pickers
                mDialogView.hourPicker.minValue = 0
                mDialogView.hourPicker.maxValue = 24
                mDialogView.minutePicker.minValue = 0
                mDialogView.minutePicker.maxValue = 60

                //handle confirm button clicked
                mDialogView.dialogConfirmBtn.setOnClickListener {

                    val targetHour = mDialogView.hourPicker.value.toString()
                    val targetMinute = mDialogView.minutePicker.value.toString()
                    val targetTime = targetHour + "h " + targetMinute + "m"

                    //dismiss dialog
                    mAlertDialog.dismiss()

                    //get reference to the on screen fragment
                    homeFragment = navHostFragment.childFragmentManager.fragments.get(0) as HomeFragment
                    info("sharedPref frag first: " + navHostFragment.childFragmentManager.fragments.get(0))
                    info("sharedPref frag: " + navHostFragment.childFragmentManager.fragments.last())

                    //update the target time
                    homeFragment.targetTimeCount.text = targetTime
                    //save target time with SharedPreferences
                    homeFragment.saveTargetTime()
                    //compare the times
                    homeFragment.compareTimeSpent(homeFragment.targetTimeCount.text.toString(),homeFragment.totalTimeCount.text.toString())

                }
                //handle cancel button clicked
                mDialogView.dialogCancelBtn.setOnClickListener {
                    //dismiss dialog
                    mAlertDialog.dismiss()
                }
            }
            R.id.item_sign_out -> {
                signOut()
            }
        }
        return item.onNavDestinationSelected(findNavController(R.id.navHostFragment))
                || super.onOptionsItemSelected(item)
    }

    // Handle up navigation behavior in the ActionBar
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.navHostFragment).navigateUp(appBarConfiguration)
    }

    private fun signOut() {
        app.auth.signOut()
        startActivity<Login>()
        finish()
    }

}