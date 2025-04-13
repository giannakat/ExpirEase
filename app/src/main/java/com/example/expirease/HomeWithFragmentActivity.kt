package com.example.expirease

import android.content.Intent
import android.graphics.Color
import android.media.Image
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.example.expirease.fragment.CalendarFragment
import com.example.expirease.fragment.HistoryFragment
import com.example.expirease.fragment.HomeFragment
import com.example.expirease.fragment.HouseholdFragment
import com.example.expirease.fragment.SettingsFragment
import com.google.android.material.navigation.NavigationView
import androidx.core.graphics.drawable.DrawableCompat


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

        val navView: NavigationView = findViewById(R.id.nav_view)
        val menu = navView.menu
        val logoutItem = menu.findItem(R.id.nav_logout)
        val spanString = SpannableString(logoutItem.title)
        spanString.setSpan(ForegroundColorSpan(Color.RED), 0, spanString.length, 0)
        logoutItem.title = spanString

        val icon = logoutItem.icon
        icon?.let {
            val wrapped = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapped, Color.RED)
            logoutItem.icon = wrapped
        }


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

        val headerView = navView.getHeaderView(0)
        val profileHeader = headerView.findViewById<LinearLayout>(R.id.profile)

        profileHeader.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            drawerLayout.closeDrawers() // Close the drawer after clicking
        }

        navView.setNavigationItemSelectedListener { menuItem ->

            if(menuItem.itemId == R.id.nav_logout){
                showLogoutDialog()
                return@setNavigationItemSelectedListener true
            }

            val newFragment: Fragment = when (menuItem.itemId) {
                R.id.nav_home -> HomeFragment()
                R.id.nav_household -> HouseholdFragment()
                R.id.nav_category -> HistoryFragment()
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

    fun showLogoutDialog() {
        // Create a dialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")

        // Set the "Logout" button
        builder.setPositiveButton("Logout") { _, _ ->
            performLogout()  // Call the logout function when user clicks "Logout"
        }

        // Set the "Cancel" button
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.dismiss()  // Dismiss dialog when user clicks "Cancel"
        }

        // Create the alert dialog
        val dialog = builder.create()

        // Show the dialog
        dialog.show()

        // Find the "Logout" button and change its text color to red
        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(Color.RED)  // Change text color to red

        // Position the "Logout" button lower (by adjusting the layout parameters)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = 50  // Adjust the top margin to push the button lower (you can tweak this value)
        positiveButton.layoutParams = layoutParams

        // Optionally adjust the "Cancel" button text color to something different if desired
        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(Color.BLACK)  // Keep the "Cancel" button with default black text
    }

    private fun performLogout() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}