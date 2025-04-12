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
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.example.expirease.app.MyApplication
import isNotValid

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        val loginTextView: TextView = findViewById(R.id.loginTextView)
        val signupButton: Button = findViewById(R.id.signup_button)
        val nameText: EditText = findViewById(R.id.create_name)
        val emailText: EditText = findViewById(R.id.create_email)
        val passwordField: EditText = findViewById(R.id.create_password)
        val confirmPasswordField: EditText = findViewById(R.id.confirm_password)

        signupButton.setOnClickListener {
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            // Check if any field is empty
            if (nameText.isNotValid() || passwordField.isNotValid() || emailText.isNotValid() || confirmPasswordField.isNotValid()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check password length
            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check if passwords match
            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords are not the same", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Successful registration
            Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_LONG).show()
            (application as MyApplication).username = nameText.text.toString()
            (application as MyApplication).password = password
            (application as MyApplication).email = emailText.text.toString()

            startActivity(Intent(this, LoginActivity::class.java))
        }

        // Create a SpannableString to make "Login" clickable
        val text = "Already have an account? Login"
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf("Login")
        val endIndex = startIndex + "Login".length // Fix typo: "Sign in" → "Login"
        spannableString.setSpan(ForegroundColorSpan(Color.GREEN), startIndex, endIndex, 0)

        spannableString.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }, startIndex, endIndex, 0)

        loginTextView.text = spannableString
        loginTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
