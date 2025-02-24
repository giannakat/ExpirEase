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


class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        val loginTextView: TextView = findViewById(R.id.loginTextView)

        // Create a SpannableString to make "Sign up" a different color and clickable
        val text = "Already have an account? Sign in"
        val spannableString = SpannableString(text)

        // Change the color of the "Sign up" text to blue (or any color you prefer)
        val startIndex = text.indexOf("Sign in")
        val endIndex = startIndex + "Sign in".length
        spannableString.setSpan(ForegroundColorSpan(Color.GREEN), startIndex, endIndex, 0)

        // Make "Sign up" clickable
        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                // Navigate to Sign Up Activity when clicked
                val intent = Intent(this@RegisterActivity, LoginActivity::class.java)
                startActivity(intent)
            }
        }, startIndex, endIndex, 0)

        // Set the SpannableString to the TextView
        loginTextView.text = spannableString

        // Make the TextView clickable
        loginTextView.movementMethod = LinkMovementMethod.getInstance()



    }
}