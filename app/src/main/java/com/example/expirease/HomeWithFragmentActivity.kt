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
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.core.content.ContextCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.expirease.viewmodel.SharedItemViewModel

class HomeWithFragmentActivity : AppCompatActivity() {
    private lateinit var sharedItemViewModel: SharedItemViewModel

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var db: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_with_fragment)

        sharedItemViewModel = ViewModelProvider(this)[SharedItemViewModel::class.java]

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
        supportActionBar?.setDisplayShowTitleEnabled(false)

        toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        updateToolbarStyleForFragment(HomeFragment())

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
                //change design of toolbar
                supportFragmentManager.executePendingTransactions()
                updateToolbarStyleForFragment(newFragment)
            }

            drawerLayout.closeDrawers()
            true
        }

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

        // Update the header with the username
        updateNavHeader()
    }

    // Method to update the navigation header with the name
    private fun updateNavHeader() {
        val headerView = navView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.username) // Assuming you have a TextView with id "username" in your layout
        val profileImageView = headerView.findViewById<ImageView>(R.id.profilepic) // Add this for profile image (make sure your header layout has an ImageView)

        // Get the current user from FirebaseAuth
        val user = auth.currentUser
        if (user != null) {
            val userRef = reference.child(user.uid)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val dataSnapshot = task.result

                    // Fetch the name (or use "USER" if null or empty)
                    val name = dataSnapshot?.child("name")?.value?.toString()?.takeIf { it.isNotEmpty() } ?: "USER"
                    nameTextView.text = name

                    // Fetch and decode the Base64 profile image
                    val base64Image = dataSnapshot?.child("profileImage")?.value?.toString()

                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            profileImageView.setImageResource(R.drawable.img_placeholder_user) // fallback image if decode fails
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.img_placeholder_user) // fallback image if no image in database
                    }
                } else {
                    Log.e("HomeWithFragmentActivity", "Failed to get name or image", task.exception)
                    nameTextView.text = "USER"
                    profileImageView.setImageResource(R.drawable.img_placeholder_user)
                }
            }
        } else {
            nameTextView.text = "USER"
            profileImageView.setImageResource(R.drawable.img_placeholder_user)
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
                sharedItemViewModel.saveItemsToFirebase {
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

    fun updateToolbarStyleForFragment(fragment: Fragment) {
        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        val toolbarTitle: TextView = findViewById(R.id.toolbar_title)
        val notifIcon: ImageView = findViewById(R.id.notif_icon)

        when (fragment) {
            is HomeFragment -> {
                toolbar.setBackgroundColor(Color.TRANSPARENT)
                toolbar.elevation = 0f
                toolbarTitle.text = "ExpirEase"
                toolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.green)) // for better contrast on transparent bg
                notifIcon.setColorFilter(ContextCompat.getColor(this, R.color.green))
                toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.green)
            }

            is HouseholdFragment -> {
                toolbar.setBackgroundColor(getColor(R.color.green))
                toolbar.elevation = 8f
                toolbarTitle.text = "Household"
                toolbarTitle.setTextColor(Color.WHITE)
                notifIcon.setColorFilter(Color.WHITE)
                toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
            }

            is HistoryFragment -> {
                toolbar.setBackgroundColor(getColor(R.color.green))
                toolbar.elevation = 8f
                toolbarTitle.text = "History"
                toolbarTitle.setTextColor(Color.WHITE)
                notifIcon.setColorFilter(Color.WHITE)
                toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
            }

            is SettingsFragment -> {
                toolbar.setBackgroundColor(getColor(R.color.green))
                toolbar.elevation = 8f
                toolbarTitle.text = "Settings"
                toolbarTitle.setTextColor(Color.WHITE)
                notifIcon.setColorFilter(Color.WHITE)
                toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
            }

            is CalendarFragment -> {
                toolbar.setBackgroundColor(getColor(R.color.green))
                toolbar.elevation = 8f
                toolbarTitle.text = "Calendar"
                toolbarTitle.setTextColor(Color.WHITE)
                notifIcon.setColorFilter(Color.WHITE)
                toggle.drawerArrowDrawable.color = ContextCompat.getColor(this, R.color.white)
            }
        }
    }

}
