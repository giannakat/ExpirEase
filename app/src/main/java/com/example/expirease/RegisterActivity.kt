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
import android.widget.Toast
import com.example.expirease.app.MyApplication
import com.example.expirease.data.Users
import com.example.expirease.databinding.ActivityRegisterBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import isNotValid

class RegisterActivity : Activity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var db: FirebaseDatabase
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = FirebaseDatabase.getInstance()
        reference = db.getReference("Users")

        val loginTextView = binding.loginTextView
        val signupButton = binding.signupButton
        val nameText = binding.createName
        val emailText = binding.createEmail
        val passwordField = binding.createPassword
        val confirmPasswordField = binding.confirmPassword

        signupButton.setOnClickListener {
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (nameText.isNotValid() || passwordField.isNotValid() || emailText.isNotValid() || confirmPasswordField.isNotValid()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords are not the same", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Add user to Firebase
            val userId = reference.push().key
            val user = Users(nameText.text.toString(), emailText.text.toString(), password)

            if (userId != null) {
                reference.child(userId).setValue(user).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(this, "Registered Successfully!", Toast.LENGTH_LONG).show()
                        (application as MyApplication).username = nameText.text.toString()
                        (application as MyApplication).password = password
                        (application as MyApplication).email = emailText.text.toString()
                        startActivity(Intent(this, LoginActivity::class.java))
                    } else {
                        Toast.makeText(this, "Failed to register in Firebase", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }

        // Create a SpannableString to make "Login" clickable
        val text = "Already have an account? Login"
        val spannableString = SpannableString(text)

        val startIndex = text.indexOf("Login")
        val endIndex = startIndex + "Login".length
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
