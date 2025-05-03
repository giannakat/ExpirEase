package com.example.expirease

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.expirease.app.MyApplication
import com.example.expirease.fragment.*
import com.example.expirease.viewmodel.SharedItemViewModel
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

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

        // Load initial fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, HomeFragment())
                .commit()
        }

        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        val menuItem = navView.menu.findItem(R.id.nav_logout)
        menuItem.icon = ContextCompat.getDrawable(this, R.drawable.ic_nav_logout)

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

        findViewById<ImageView>(R.id.notif_icon).setOnClickListener {
            startActivity(Intent(this, NotificationsActivity::class.java))
        }

        val headerView = navView.getHeaderView(0)
        headerView.findViewById<LinearLayout>(R.id.profile).setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
            drawerLayout.closeDrawers()
        }

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
                supportFragmentManager.executePendingTransactions()
                updateToolbarStyleForFragment(newFragment)
            }

            drawerLayout.closeDrawers()
            true
        }

        // Make logout red
        val logoutItem = navView.menu.findItem(R.id.nav_logout)
        val spanString = SpannableString(logoutItem.title)
        spanString.setSpan(ForegroundColorSpan(Color.RED), 0, spanString.length, 0)
        logoutItem.title = spanString
        logoutItem.icon?.let {
            val wrapped = DrawableCompat.wrap(it)
            DrawableCompat.setTint(wrapped, Color.RED)
            logoutItem.icon = wrapped
        }

        updateNavHeader()
    }

    // Ensure header is refreshed every time activity resumes (like after returning from profile)
    override fun onResume() {
        super.onResume()
        updateNavHeader()
    }

    private fun updateNavHeader() {
        val headerView = navView.getHeaderView(0)
        val nameTextView = headerView.findViewById<TextView>(R.id.username)
        val emailTextView = headerView.findViewById<TextView>(R.id.email)
        val profileImageView = headerView.findViewById<ImageView>(R.id.profilepic)

        val user = auth.currentUser
        if (user != null) {
            val userRef = reference.child(user.uid)
            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result

                    val name = data?.child("name")?.value?.toString()?.takeIf { it.isNotEmpty() } ?: "USER"
                    var email = data?.child("email")?.value?.toString()

                    // Fallback if email is missing in database
                    if (email.isNullOrEmpty()) {
                        email = user.email ?: "Not Available"
                        userRef.child("email").setValue(email)  // Save email to database
                    }

                    nameTextView.text = name
                    emailTextView.text = email

                    val base64Image = data?.child("profileImage")?.value?.toString()
                    if (!base64Image.isNullOrEmpty()) {
                        try {
                            val decodedBytes = Base64.decode(base64Image, Base64.DEFAULT)
                            val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            profileImageView.setImageBitmap(bitmap)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            profileImageView.setImageResource(R.drawable.img_placeholder_user)
                        }
                    } else {
                        profileImageView.setImageResource(R.drawable.img_placeholder_user)
                    }
                } else {
                    Log.e("NavHeader", "Error fetching user data", task.exception)
                    nameTextView.text = "USER"
                    emailTextView.text = "Not Available"
                    profileImageView.setImageResource(R.drawable.img_placeholder_user)
                }
            }
        } else {
            nameTextView.text = "USER"
            emailTextView.text = "Not Available"
            profileImageView.setImageResource(R.drawable.img_placeholder_user)
        }
    }

    fun showLogoutDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Logout")
        builder.setMessage("Are you sure you want to log out?")
        builder.setPositiveButton("Logout") { _, _ ->
            Toast.makeText(this, "Logging out...", Toast.LENGTH_SHORT).show()

            val app = application as MyApplication
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                sharedItemViewModel.saveItemsToFirebase {
                    FirebaseAuth.getInstance().signOut()
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    })
                    finish()
                }
            } else {
                startActivity(Intent(this, LoginActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
                finish()
            }
        }

        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }

        val dialog = builder.create()
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
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
                toolbarTitle.setTextColor(ContextCompat.getColor(this, R.color.green))
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
