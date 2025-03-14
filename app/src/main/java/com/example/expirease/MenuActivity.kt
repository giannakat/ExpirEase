package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expirease.app.MyApplication

class MenuActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        val homeButton = findViewById<LinearLayout>(R.id.home)  // Ensure ID exists in XML
        val settingsButton = findViewById<LinearLayout>(R.id.Settings)
        val logoutButton = findViewById<LinearLayout>(R.id.logout)
        val profileButton = findViewById<ImageView>(R.id.nav_profilepicture)
        val navBackButton = findViewById<ImageView>(R.id.nav_back)

        val edittext_name : EditText = findViewById<EditText>(R.id.nav_name)
        val edittext_email : EditText = findViewById<EditText>(R.id.nav_email)

        edittext_name.setText((application as MyApplication).name)
        edittext_email.setText((application as MyApplication).email)

        navBackButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
       homeButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }
        logoutButton.setOnClickListener {
            val intent = Intent(this, LogoutActivity::class.java)
            startActivity(intent)
        }
        profileButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}