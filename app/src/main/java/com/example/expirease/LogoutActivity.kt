package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LogoutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logout)

        val noButton: Button = findViewById(R.id.reverse_button)
        val yesButton: Button = findViewById(R.id.yes_button)

        noButton.setOnClickListener {
            val intent = Intent(this, HomeWithFragmentActivity::class.java)
            startActivity(intent)
        }

        yesButton.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }
}