// HomeWithFragmentActivity.kt
package com.example.expirease

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.DrawableCompat
import com.example.expirease.app.MyApplication
import com.example.expirease.fragment.*
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.auth.FirebaseUser
import android.content.Intent
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment

class HomeWithFragmentActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var db: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_with_fragment)

        db = FirebaseDatabase.getInstance()
        reference = db.getReference("Users")
        auth = FirebaseAuth.getInstance()

        // Load HomeFragment initially
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Logout item styling
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

        // Notification button
        val notifButton: ImageView = findViewById(R.id.notif_icon)
        notifButton.setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        // Profile header click
        val headerView = navView.getHeaderView(0)
        val profileHeader = headerView.findViewById<LinearLayout>(R.id.profile)
        profileHeader.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            drawerLayout.closeDrawers()
        }

        // Navigation item click listener
        navView.setNavigationItemSelectedListener { menuItem ->
            if (menuItem.itemId == R.id.nav_logout) {
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
            if (currentFragment?.javaClass != newFragment.javaClass) {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, newFragment)
                    .commit()
            }

            drawerLayout.closeDrawers()
            true
        }
    }

    // Handle logout dialog
    fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")

        builder.setPositiveButton("Logout") { _, _ ->
            Log.d("Logout", "Logout button clicked")
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            val app = application as MyApplication
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                Log.d("Logout", "User is authenticated. UID: ${user.uid}")

                // Calling saveItemsToFirebase from MyApplication
                app.saveItemsToFirebase {
                    Log.d("Logout", "Items saved. Proceeding to logout.")
                    FirebaseAuth.getInstance().signOut()
                    Log.d("Logout", "User signed out")

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                }
            } else {
                Log.d("Logout", "No authenticated user. Skipping Firebase save.")

                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            Log.d("Logout", "Logout canceled")
            dialog.dismiss()
        }

        val dialog = builder.create()
        dialog.show()

        val positiveButton = dialog.getButton(AlertDialog.BUTTON_POSITIVE)
        positiveButton.setTextColor(Color.RED)
        val layoutParams = positiveButton.layoutParams as LinearLayout.LayoutParams
        layoutParams.topMargin = 50
        positiveButton.layoutParams = layoutParams

        val negativeButton = dialog.getButton(AlertDialog.BUTTON_NEGATIVE)
        negativeButton.setTextColor(Color.BLACK)
    }
}
