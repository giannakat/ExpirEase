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
import androidx.appcompat.app.AppCompatActivity
import com.example.expirease.app.MyApplication
import com.example.expirease.data.Users
import com.example.expirease.databinding.ActivityLoginBinding // Import the generated binding class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase


class LoginActivity : AppCompatActivity() {

    private lateinit var db: FirebaseDatabase
    private lateinit var reference: DatabaseReference
    private lateinit var auth: FirebaseAuth // Firebase Auth instance

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        val binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)  // Set the content view to the root of the binding object

        // Initialize Firebase instances
        auth = FirebaseAuth.getInstance()
        db = FirebaseDatabase.getInstance()
        reference = FirebaseDatabase.getInstance().getReference("Users")

        // Access views via binding object
        val loginButton = binding.btnlogin
        val signUpTextView = binding.signUpTextView
        val edittext_username = binding.edittextUsername
        val edittext_password = binding.edittextPassword

        // Custom class for passing data (if using shared app-level data)
        edittext_username.setText((application as MyApplication).username)
        edittext_password.setText((application as MyApplication).password)

        // Login button validation
        loginButton.setOnClickListener {
            val username = edittext_username.text.toString()
            val password = edittext_password.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username or password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            // Check if username and password match Firebase credentials
            authenticateUser(username, password)
        }

        // Move sign-up text configuration outside the button click event
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

    private fun authenticateUser(username: String, password: String) {
        // Retrieve user data from Firebase Realtime Database
        reference.orderByChild("username").equalTo(username).get().addOnSuccessListener { dataSnapshot ->
            if (dataSnapshot.exists()) {
                val user = dataSnapshot.children.first().getValue(Users::class.java)
                if (user != null && user.password == password) {
                    // Credentials match, proceed to the home activity
                    Toast.makeText(this, "Login Successful!", Toast.LENGTH_LONG).show()
                    val intent = Intent(this, HomeWithFragmentActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    // Incorrect password
                    Toast.makeText(this, "Incorrect username or password", Toast.LENGTH_LONG).show()
                }
            } else {
                // No such username found
                Toast.makeText(this, "User not found", Toast.LENGTH_LONG).show()
            }
        }.addOnFailureListener { exception ->
            // Handle any errors that occur while querying the database
            Toast.makeText(this, "Error fetching data from Firebase", Toast.LENGTH_LONG).show()
            exception.printStackTrace()
        }
    }

}
