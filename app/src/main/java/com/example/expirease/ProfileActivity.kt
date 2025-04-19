package com.example.expirease

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
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

    private lateinit var sharedPrefs: SharedPreferences

    // Image picker using OpenDocument
    private val pickImage = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                accountIcon.setImageURI(it)
                saveImageUri(it.toString())
            } catch (e: SecurityException) {
                Toast.makeText(this, "Failed to access image.", Toast.LENGTH_SHORT).show()
                e.printStackTrace()
            }
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                pickImage.launch(arrayOf("image/*"))
            } else {
                Toast.makeText(this, "Permission denied!", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPrefs = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

        // UI refs
        accountIcon = findViewById(R.id.accountIcon)
        val editPhotoLayout = findViewById<LinearLayout>(R.id.editPhoto)
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

        loadProfileData()

        editPhotoLayout.setOnClickListener {
            if (hasStoragePermission()) {
                pickImage.launch(arrayOf("image/*"))
            } else {
                requestStoragePermission()
            }
        }

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
        etPhone.setText(sharedPrefs.getString("phone", ""))

        sharedPrefs.getString("imageUri", null)?.let { uriString ->
            val uri = Uri.parse(uriString)

            val persistedUris = contentResolver.persistedUriPermissions
            val hasPermission = persistedUris.any { it.uri == uri && it.isReadPermission }

            if (hasPermission) {
                try {
                    accountIcon.setImageURI(uri)
                } catch (e: SecurityException) {
                    e.printStackTrace()
                    Toast.makeText(this, "Image can't be loaded (no permission).", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Image access expired or revoked.", Toast.LENGTH_SHORT).show()
            }
        }
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
        return if (
            etName.text.isNotEmpty() &&
            etUsername.text.isNotEmpty() &&
            etEmail.text.isNotEmpty() &&
            etPhone.text.isNotEmpty() &&
            etPassword.text.isNotEmpty()
        ) {
            true
        } else {
            Toast.makeText(this, "All fields must be filled before saving!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun saveChanges() {
        val app = application as MyApplication
        app.username = etUsername.text.toString()
        app.password = etPassword.text.toString()
        app.email = etEmail.text.toString()
        app.name = etName.text.toString()

        with(sharedPrefs.edit()) {
            putString("phone", etPhone.text.toString())
            apply()
        }
    }

    private fun saveImageUri(uri: String) {
        sharedPrefs.edit().putString("imageUri", uri).apply()
    }

    private fun hasStoragePermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES) == PackageManager.PERMISSION_GRANTED
        } else {
            ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }
}
