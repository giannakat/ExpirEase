package com.example.expirease

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast

class ProfileActivity : Activity() {
    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var editNameIcon: ImageView
    private lateinit var editUsernameIcon: ImageView
    private lateinit var editEmailIcon: ImageView
    private lateinit var editPhoneIcon: ImageView
    private lateinit var editPasswordIcon: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // Initialize SharedPreferences
        sharedPreferences = getSharedPreferences("UserProfile", MODE_PRIVATE)

        // Initialize UI elements
        etName = findViewById(R.id.nameValue)
        etUsername = findViewById(R.id.usernameValue)
        etEmail = findViewById(R.id.emailValue)
        etPhone = findViewById(R.id.phoneValue)
        etPassword = findViewById(R.id.passwordValue)

        editNameIcon = findViewById(R.id.editName)
        editUsernameIcon = findViewById(R.id.editUsername)
        editEmailIcon = findViewById(R.id.editEmail)
        editPhoneIcon = findViewById(R.id.editPhone)
        editPasswordIcon = findViewById(R.id.editPassword)

        btnSave = findViewById(R.id.saveProfileButton)
        btnBack = findViewById(R.id.back_button)

        // Load saved profile data
        loadProfileData()

        // Set click listeners for editing fields
        editNameIcon.setOnClickListener { enableEditing(etName) }
        editUsernameIcon.setOnClickListener { enableEditing(etUsername) }
        editEmailIcon.setOnClickListener { enableEditing(etEmail) }
        editPhoneIcon.setOnClickListener { enableEditing(etPhone) }
        editPasswordIcon.setOnClickListener { enableEditing(etPassword) }

        // Save button
        btnSave.setOnClickListener {
            if (validateFields()) {
                saveChanges()
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        // Back button
        btnBack.setOnClickListener {
            startActivity(
                Intent(this, MenuActivity::class.java).apply{
                    putExtra("name", etName.text.toString())
                    putExtra("email", etEmail.text.toString())
                }
            )
        }
    }

    // Load previously saved data
    private fun loadProfileData() {
        etName.setText(sharedPreferences.getString("name", "Gianna Carreon"))
        etUsername.setText(sharedPreferences.getString("username", "Gianna123"))
        etEmail.setText(sharedPreferences.getString("email", "gianna@example.com"))
        etPhone.setText(sharedPreferences.getString("phone", "09123456789"))
        etPassword.setText(sharedPreferences.getString("password", "****"))

    }

    // Enable editing of an EditText field (ALWAYS ALLOW EDITING)
    private fun enableEditing(editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isCursorVisible = true
        editText.requestFocus()

        // Ensure cursor appears in an empty field
        if (editText.text.toString().isEmpty()) {
            editText.setSelection(0)
        }

        // Show keyboard
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }



    // Validate if all fields have values
    private fun validateFields(): Boolean {
        return if (etName.text.toString().isNotEmpty() &&
            etUsername.text.toString().isNotEmpty() &&
            etEmail.text.toString().isNotEmpty() &&
            etPhone.text.toString().isNotEmpty() &&
            etPassword.text.toString().isNotEmpty()
        ) {
            true
        } else {
            Toast.makeText(this, "All fields must be filled before saving!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    // Save changes to SharedPreferences
    private fun saveChanges() {
        val editor = sharedPreferences.edit()
        editor.putString("name", etName.text.toString())
        editor.putString("username", etUsername.text.toString())
        editor.putString("email", etEmail.text.toString())
        editor.putString("phone", etPhone.text.toString())
        editor.putString("password", etPassword.text.toString())
        editor.apply()
    }
}
