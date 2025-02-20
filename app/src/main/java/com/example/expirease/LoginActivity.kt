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


        val login_button : Button = findViewById<Button>(R.id.btnlogin)
        login_button.setOnClickListener{
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


        val signUpTextView: TextView = findViewById(R.id.signUpTextView)

        // Create a SpannableString to make "Sign up" a different color and clickable
        val text = "Don't have an Account? Sign up"
        val spannableString = SpannableString(text)

        // Change the color of the "Sign up" text to blue (or any color you prefer)
        val startIndex = text.indexOf("Sign up")
        val endIndex = startIndex + "Sign up".length
        spannableString.setSpan(ForegroundColorSpan(Color.GREEN), startIndex, endIndex, 0)

        // Make "Sign up" clickable
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Sign Up Activity when clicked
                val intent = Intent(this@LoginActivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        }, startIndex, endIndex, 0)

        // Set the SpannableString to the TextView
        signUpTextView.text = spannableString

        // Make the TextView clickable
        signUpTextView.movementMethod = LinkMovementMethod.getInstance()


    }
}