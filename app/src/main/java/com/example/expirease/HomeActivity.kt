package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import toast

class HomeActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private  lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)


        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        //initialize views
        drawerLayout = findViewById(R.id.drawer_layout)
        navView = findViewById(R.id.nav_view)

        //val menuButton: ImageView = findViewById(R.id.menu_button)

//        menuButton.setOnClickListener {
//            val intent = Intent(this, MenuActivity::class.java)
//            startActivity(intent)
//        }


        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()


        //handle navigation menu item on click
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId){
                R.id.nav_home -> toast("Home selected")
                R.id.nav_calendar -> toast("Calendar selected")
                R.id.nav_settings -> toast("Settings selected")
                R.id.nav_household -> toast("Household members selected")
            }
            drawerLayout.closeDrawers()
            true
        }



    }
}