package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView

class HouseholdActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_household)

        val backButton: ImageView = findViewById(R.id.back_button)

        backButton.setOnClickListener {
            val intent = Intent(this, MenuActivity::class.java)
            startActivity(intent)
        }
    }
}