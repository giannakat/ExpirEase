package com.example.expirease

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
import com.example.expirease.data.Users
import com.example.expirease.databinding.ActivityLoginBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().getReference("Users")

        val loginButton = binding.btnlogin
        val usernameField = binding.edittextUsername
        val passwordField = binding.edittextPassword

        loginButton.setOnClickListener {
            val username = usernameField.text.toString()
            val password = passwordField.text.toString()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Username or password cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            reference.orderByChild("username").equalTo(username)
                .get()
                .addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        val user = snapshot.children.first().getValue(Users::class.java)
                        val email = user?.email
                        if (email != null) {
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        Toast.makeText(this, "Login Successful!", Toast.LENGTH_SHORT).show()
                                        startActivity(Intent(this, HomeWithFragmentActivity::class.java))
                                        finish()
                                    } else {
                                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        } else {
                            Toast.makeText(this, "User email not found", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this, "Username not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Database error", Toast.LENGTH_SHORT).show()
                }
        }

        val text = SpannableString("Don't have an Account? Sign up")
        val start = text.indexOf("Sign up")
        val end = start + "Sign up".length

        text.setSpan(ForegroundColorSpan(Color.GREEN), start, end, 0)
        text.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@LoginActivity, RegisterActivity::class.java))
            }
        }, start, end, 0)

        binding.signUpTextView.text = text
        binding.signUpTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
