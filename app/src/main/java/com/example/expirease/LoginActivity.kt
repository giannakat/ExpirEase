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
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val loginButton: Button = findViewById(R.id.btnlogin)
        val signUpTextView: TextView = findViewById(R.id.signUpTextView)
        val edittext_username : EditText = findViewById<EditText>(R.id.edittext_username)
        val edittext_password : EditText = findViewById<EditText>(R.id.edittext_password)

        intent?.let{
            it.getStringExtra("nameText")?.let {username->
                edittext_username.setText(username)
            }
            it.getStringExtra("passwordField")?.let {password->
                edittext_password.setText(password)
            }
        }

        loginButton.setOnClickListener {
            val username = edittext_username.text.toString()
            val password = edittext_password.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username or password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (username == "gianna" && password == "123") {
                Toast.makeText(this, "Username and password are correct", Toast.LENGTH_LONG).show()
                val intent = Intent(this, MainActivity::class.java)
                startActivity(intent) // ✅ Only start ProfileActivity if login is correct
            } else {
                Toast.makeText(this, "Username and password are incorrect", Toast.LENGTH_LONG).show()
            }
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
