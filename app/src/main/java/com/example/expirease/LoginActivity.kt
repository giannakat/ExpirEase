package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.Button
import android.widget.TextView

class LoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.btnlogin)
        val signUpTextView: TextView = findViewById(R.id.signUpTextView)

        // ✅ Fix: Set click listener properly for login button
        loginButton.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent) // ✅ Start the new activity
        }

        // ✅ Move sign-up text configuration outside the button click event
        val text = "Don't have an Account? Sign up"
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf("Sign up")
        val endIndex = startIndex + "Sign up".length

        // Change text color
        spannableString.setSpan(ForegroundColorSpan(Color.GREEN), startIndex, endIndex, 0)

        // Make it clickable
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }, startIndex, endIndex, 0)

        // Set the SpannableString to the TextView
        signUpTextView.text = spannableString
        signUpTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
