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
import com.example.expirease.app.MyApplication

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

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
                Intent(this, HomeWithFragmentActivity::class.java)
            )
        }
    }

    // Load previously saved data
    private fun loadProfileData() {
        etName.setText((application as MyApplication).name)
        etEmail.setText((application as MyApplication).email)
        etUsername.setText((application as MyApplication).username)
        etPassword.setText((application as MyApplication).password)
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
        (application as MyApplication).username = etUsername.text.toString()
        (application as MyApplication).password = etPassword.text.toString()
        (application as MyApplication).email = etEmail.text.toString()
        (application as MyApplication).name = etName.text.toString()
    }
}
