package com.example.expirease

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
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
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeWithFragmentActivity::class.java))
            finish()
            return
        }
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        reference = FirebaseDatabase.getInstance().getReference("Users")
        auth = FirebaseAuth.getInstance()

        // 🔐 Ask for notification permission (Android 13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }

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
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()) {
                            val userSnapshot = snapshot.children.first()
                            val user = userSnapshot.getValue(Users::class.java)
                            val email = user?.email

                            if (email != null) {
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val userId = auth.currentUser?.uid
                                            if (userId != null) {
                                                FirebaseMessaging.getInstance().token
                                                    .addOnCompleteListener { tokenTask ->
                                                        if (tokenTask.isSuccessful) {
                                                            val token = tokenTask.result
                                                            reference.child(userId).child("deviceToken").setValue(token)
                                                        }
                                                    }
                                            }

                                            Toast.makeText(this@LoginActivity, "Login Successful!", Toast.LENGTH_SHORT).show()
                                            startActivity(Intent(this@LoginActivity, HomeWithFragmentActivity::class.java))
                                            finish()
                                        } else {
                                            Toast.makeText(this@LoginActivity, "Authentication failed", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                Toast.makeText(this@LoginActivity, "User email not found", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@LoginActivity, "Username not found", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, "Database error: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // ✨ Make "Sign up" text clickable
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

    // ✅ Handle the result of the permission request
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Notifications enabled!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Notifications disabled. You won’t get alerts!", Toast.LENGTH_LONG).show()
            }
        }
    }
}
