package com.example.expirease

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.expirease.app.MyApplication
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.FileOutputStream

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

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference
    private val storage = FirebaseStorage.getInstance()

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(
                    it,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                saveImageUri(it.toString())
                saveImageToLocal(it)
                uploadImageToFirebase(it)
                loadProfileImage()
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Permission denied for image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openImagePicker()
        } else {
            Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sharedPrefs = getSharedPreferences("ProfilePrefs", Context.MODE_PRIVATE)

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
        loadProfileImage()

        editPhotoLayout.setOnClickListener {
            checkAndRequestPermission()
        }

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
    }

    private fun loadProfileImage() {
        val uriString = sharedPrefs.getString("imageUri", null)
        val file = File(filesDir, "profile_image.jpg")

        val hasLocalUriPermission = uriString?.let {
            val uri = Uri.parse(it)
            contentResolver.persistedUriPermissions.any { perm -> perm.uri == uri && perm.isReadPermission }
        } ?: false

        when {
            hasLocalUriPermission && uriString != null -> {
                accountIcon.setImageURI(Uri.parse(uriString))
            }
            file.exists() -> {
                val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                accountIcon.setImageBitmap(bitmap)
            }
            else -> {
                accountIcon.setImageResource(R.drawable.img_placeholder_user)
            }
        }
    }

    private fun enableEditing(editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isCursorVisible = true
        editText.requestFocus()

        if (editText.text.isNotEmpty()) {
            editText.setSelection(editText.text.length)
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
        val uid = firebaseAuth.currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        app.username = etUsername.text.toString()
        app.password = etPassword.text.toString()
        app.email = etEmail.text.toString()
        app.name = etName.text.toString()

        val userRef = database.child("Users").child(uid)

        val updatedUser = mapOf(
            "username" to app.username,
            "password" to app.password,
            "email" to app.email,
            "name" to app.name,
            "phone" to etPhone.text.toString()
        )

        userRef.updateChildren(updatedUser).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
                saveProfileToSharedPrefs()
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveProfileToSharedPrefs() {
        sharedPrefs.edit().apply {
            putString("name", etName.text.toString())
            putString("username", etUsername.text.toString())
            putString("email", etEmail.text.toString())
            putString("phone", etPhone.text.toString())
            putString("password", etPassword.text.toString())
            apply()
        }
    }

    private fun saveImageUri(uri: String) {
        sharedPrefs.edit().putString("imageUri", uri).apply()
    }

    private fun saveImageToLocal(uri: Uri) {
        val file = File(filesDir, "profile_image.jpg")
        try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(file).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser == null) {
            Toast.makeText(this, "No authenticated user. Skipping Firebase save.", Toast.LENGTH_SHORT).show()
            Log.w("UPLOAD_IMAGE", "Skipped upload: User not authenticated")
            return
        }

        val uid = currentUser.uid
        val ref = storage.reference.child("Users/$uid/profile.jpg")

        // Upload the image to Firebase Storage
        ref.putFile(uri)
            .addOnSuccessListener {
                // On success, get the download URL and save it to Firebase Realtime Database
                ref.downloadUrl
                    .addOnSuccessListener { downloadUri ->
                        Log.d("UPLOAD_SUCCESS", "Image uploaded successfully: $downloadUri")

                        // Save the image URL to Firebase Realtime Database
                        database.child("Users").child(uid).child("profileImageUrl").setValue(downloadUri.toString())
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    Log.d("DATABASE_UPDATE", "Image URL saved successfully")
                                } else {
                                    Log.e("DATABASE_UPDATE_ERROR", "Failed to save image URL", task.exception)
                                }
                            }
                    }
                    .addOnFailureListener { e ->
                        Log.e("DOWNLOAD_URL_ERROR", "Failed to get download URL", e)
                    }
            }
            .addOnFailureListener { e ->
                Log.e("UPLOAD_ERROR", "Image upload failed", e)
            }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openImagePicker()
                }
                else -> {
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        } else {
            openImagePicker()
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }
}