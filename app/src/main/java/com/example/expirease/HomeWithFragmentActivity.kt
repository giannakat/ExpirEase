package com.example.expirease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.expirease.fragment.CalendarFragment
import com.example.expirease.fragment.CategoryFragment
import com.example.expirease.fragment.HomeFragment
import com.example.expirease.fragment.HouseholdFragment
import com.example.expirease.fragment.SettingsFragment
import com.google.android.material.navigation.NavigationView


class HomeWithFragmentActivity : AppCompatActivity() {

    private lateinit var drawerLayout:DrawerLayout
    private  lateinit var navView:NavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_with_fragment)

//        set home fragment by default
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //Create listener and setting to drawer
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

//        navView.setNavigationItemSelectedListener { menuItem ->
//            if(menuItem.itemId == R.id.nav_logout){
//                startActivity(
//                    Intent(this, LogoutActivity::class.java)
//                )
//                return@setNavigationItemSelectedListener true
//            }
//            val fragment: Fragment = when (menuItem.itemId){
//                R.id.nav_home -> HomeFragment();
//                R.id.nav_household -> HouseholdFragment()
//                R.id.nav_category -> CategoryFragment()
//                R.id.nav_settings -> SettingsFragment()
//                R.id.nav_calendar -> CalendarFragment()
//                else -> HomeFragment()
//            }
//            supportFragmentManager.beginTransaction().replace(R.id.fragment_container, fragment).commit()
//            drawerLayout.closeDrawers()
//            true
//        }

        val notifButton: ImageView = findViewById(R.id.notif_icon)

        notifButton.setOnClickListener{
            startActivity(Intent(this,NotificationsActivity::class.java))

        }

        navView.setNavigationItemSelectedListener { menuItem ->

            if(menuItem.itemId == R.id.nav_logout){
                showLogoutDialog()
                return@setNavigationItemSelectedListener true
            }

            val newFragment: Fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_household -> HouseholdFragment()
                R.id.nav_category -> CategoryFragment()
                R.id.nav_settings -> SettingsFragment()
                R.id.nav_calendar -> CalendarFragment()

                else -> HomeFragment()
            }

            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
            if (currentFragment?.javaClass != newFragment.javaClass) { // Prevent unnecessary replacements
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .commit()
            }

            drawerLayout.closeDrawers()
            true
        }


    }

    fun showLogoutDialog(){
        //create the view itself equivalent to setContent
        val builder = AlertDialog.Builder(this)  // Create a dialog builder
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")

        // Set the "Logout" button
        builder.setPositiveButton("Logout") { _, _ ->
            performLogout()  // Call logout function when user clicks "Logout"
        }

        // Set the "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()  // Dismiss dialog when user clicks "Cancel"
        }

        val dialog = builder.create()  // Create the dialog
        dialog.show()  // Show the dialog
    }

    private fun performLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}