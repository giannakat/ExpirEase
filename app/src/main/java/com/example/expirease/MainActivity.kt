package com.example.expirease

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private  lateinit var navView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


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
                Log.e("nav", "My nav drawer is called Clicked: ${menuItem.title}")
                Toast.makeText(this, "Clicked: ${menuItem.title}", Toast.LENGTH_SHORT).show()
                when (menuItem.itemId){
                    R.id.nav_home -> Toast.makeText(this, "home selected", Toast.LENGTH_LONG).show()
                    R.id.nav_calendar -> startActivity(
                        Intent(this, CalendarActivity::class.java)
                    )
                    R.id.nav_settings -> Toast.makeText(this, "home selected", Toast.LENGTH_LONG).show()
                    R.id.nav_household -> startActivity(
                        Intent(this, CustomListViewActivity::class.java)
                    )
                }
                drawerLayout.closeDrawers()
                true
            }


        }
}