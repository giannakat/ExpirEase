package com.example.expirease

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.expirease.app.MyApplication

class ProfileActivity : AppCompatActivity() {
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
    private lateinit var accountIcon: ImageView

//    private lateinit var sharedPrefs: SharedPreferences

//    private val pickImage = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
//        uri?.let {
//            accountIcon.setImageURI(it)
//            saveImageUri(it.toString())
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

//        sharedPrefs = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)
//
//        // UI
//        accountIcon = findViewById(R.id.accountIcon)
//        val editPhotoLayout = findViewById<LinearLayout>(R.id.editPhoto)
//        editPhotoLayout.setOnClickListener {
//            pickImage.launch("image/*")
//        }

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

        // Load data
        loadProfileData()

        // Enable editing
        editNameIcon.setOnClickListener { enableEditing(etName) }
        editUsernameIcon.setOnClickListener { enableEditing(etUsername) }
        editEmailIcon.setOnClickListener { enableEditing(etEmail) }
        editPhoneIcon.setOnClickListener { enableEditing(etPhone) }
        editPasswordIcon.setOnClickListener { enableEditing(etPassword) }

        btnSave.setOnClickListener {
            if (validateFields()) {
                saveChanges()
                Toast.makeText(this, "Saved successfully!", Toast.LENGTH_SHORT).show()
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeWithFragmentActivity::class.java))
        }
    }

    private fun loadProfileData() {
        val app = application as MyApplication
        etName.setText(app.name)
        etEmail.setText(app.email)
        etUsername.setText(app.username)
        etPassword.setText(app.password)
//        etPhone.setText(sharedPrefs.getString("phone", ""))

//        // Load saved image
//        sharedPrefs.getString("imageUri", null)?.let {
//            accountIcon.setImageURI(Uri.parse(it))
//        }
    }

    private fun enableEditing(editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isCursorVisible = true
        editText.requestFocus()

        if (editText.text.toString().isEmpty()) {
            editText.setSelection(0)
        }

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun validateFields(): Boolean {
        if (etName.text.isNotEmpty() &&
            etUsername.text.isNotEmpty() &&
            etEmail.text.isNotEmpty() &&
            etPhone.text.isNotEmpty() &&
            etPassword.text.isNotEmpty()
        ) {
            return true
        }
        Toast.makeText(this, "All fields must be filled before saving!", Toast.LENGTH_SHORT).show()
        return false
    }

    private fun saveChanges() {
        val app = application as MyApplication
        app.username = etUsername.text.toString()
        app.password = etPassword.text.toString()
        app.email = etEmail.text.toString()
        app.name = etName.text.toString()

        // Save phone and image URI in SharedPreferences
//        with(sharedPrefs.edit()) {
//            putString("phone", etPhone.text.toString())
//            apply()
//        }
    }

//    private fun saveImageUri(uri: String) {
//        sharedPrefs.edit().putString("imageUri", uri).apply()
//    }
}
