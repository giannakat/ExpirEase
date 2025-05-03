package com.example.expirease

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.expirease.data.Users
import com.example.expirease.databinding.ActivityRegisterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessaging

class RegisterActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegisterBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var reference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        reference = FirebaseDatabase.getInstance().getReference("Users")

        val nameField = binding.createName
        val emailField = binding.createEmail
        val passwordField = binding.createPassword
        val confirmPasswordField = binding.confirmPassword
        val signupButton = binding.signupButton


        var isPasswordVisible1 = false
        var isPasswordVisible2 = false

        val passwordEditText: EditText = findViewById(R.id.create_password)
        val togglePassword1: ImageView = findViewById(R.id.iv_toggle_password1)
        val confirmPasswordEditText: EditText = findViewById(R.id.confirm_password)
        val togglePassword2: ImageView = findViewById(R.id.iv_toggle_password2)

        togglePassword1.setOnClickListener {
            isPasswordVisible1 = !isPasswordVisible1
            if (isPasswordVisible1) {
                // Show password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword1.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Hide password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword1.setImageResource(R.drawable.ic_eye_closed)
            }
            // Keep cursor at the end
            passwordEditText.setSelection(passwordEditText.text.length)
        }

        togglePassword2.setOnClickListener {
            isPasswordVisible2 = !isPasswordVisible2
            if (isPasswordVisible2) {
                // Show password
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePassword2.setImageResource(R.drawable.ic_eye_open)
            } else {
                // Hide password
                confirmPasswordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePassword2.setImageResource(R.drawable.ic_eye_closed)
            }
            // Keep cursor at the end
            passwordEditText.setSelection(confirmPasswordEditText.text.length)
        }

        signupButton.setOnClickListener {
            val username = nameField.text.toString()
            val email = emailField.text.toString()
            val password = passwordField.text.toString()
            val confirmPassword = confirmPasswordField.text.toString()

            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Toast.makeText(this, "Password must be at least 8 characters", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val currentUser = auth.currentUser
                        val userId = currentUser?.uid
                        if (userId != null) {
                            val user = Users(username, email, password) // ⚠️ Reminder: don't store passwords like this in production

                            reference.child(userId).setValue(user).addOnCompleteListener { dbTask ->
                                if (dbTask.isSuccessful) {
                                    FirebaseMessaging.getInstance().token.addOnCompleteListener { tokenTask ->
                                        if (tokenTask.isSuccessful) {
                                            val token = tokenTask.result
                                            reference.child(userId).child("deviceToken").setValue(token)
                                        }
                                    }

                                    Toast.makeText(this, "Registration successful!", Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this, HomeWithFragmentActivity::class.java))
                                    finish()
                                } else {
                                    Toast.makeText(this, "Failed to save user data", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Firebase Auth failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                    }
                }
        }

        // Setup clickable login text
        val loginText = SpannableString("Already have an account? Login")
        val start = loginText.indexOf("Login")
        val end = start + "Login".length

        loginText.setSpan(ForegroundColorSpan(Color.GREEN), start, end, 0)
        loginText.setSpan(object : ClickableSpan() {
            override fun onClick(widget: View) {
                startActivity(Intent(this@RegisterActivity, LoginActivity::class.java))
            }
        }, start, end, 0)

        binding.loginTextView.text = loginText
        binding.loginTextView.movementMethod = LinkMovementMethod.getInstance()
    }
}
