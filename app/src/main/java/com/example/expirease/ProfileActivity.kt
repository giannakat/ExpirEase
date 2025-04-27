package com.example.expirease

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.InputStream

class ProfileActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var etUsername: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var editNameIcon: ImageView
    private lateinit var editUsernameIcon: ImageView
    private lateinit var editPhoneIcon: ImageView
    private lateinit var editPasswordIcon: ImageView
    private lateinit var btnSave: Button
    private lateinit var btnBack: Button
    private lateinit var accountIcon: ImageView
    private lateinit var sharedPrefs: SharedPreferences

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            saveImageToDatabase(it)
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
        etPhone = findViewById(R.id.phoneValue)
        etPassword = findViewById(R.id.passwordValue)
        editNameIcon = findViewById(R.id.editName)
        editUsernameIcon = findViewById(R.id.editUsername)
        editPhoneIcon = findViewById(R.id.editPhone)
        editPasswordIcon = findViewById(R.id.editPassword)
        btnSave = findViewById(R.id.saveProfileButton)
        btnBack = findViewById(R.id.back_button)

        fetchUserDataFromFirebase()
        loadProfileImage()

        editPhotoLayout.setOnClickListener {
            checkAndRequestPermission()
        }

        editNameIcon.setOnClickListener { enableEditing(etName) }
        editUsernameIcon.setOnClickListener { enableEditing(etUsername) }
        editPhoneIcon.setOnClickListener { enableEditing(etPhone) }
        editPasswordIcon.setOnClickListener { enableEditing(etPassword) }

        btnSave.setOnClickListener {
            if (validateFields()) {
                saveChanges()
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomeWithFragmentActivity::class.java))
        }
    }

    private fun fetchUserDataFromFirebase() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val userRef = database.child("Users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                etName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                etUsername.setText(snapshot.child("username").getValue(String::class.java) ?: "")
                etPhone.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
                etPassword.setText(snapshot.child("password").getValue(String::class.java) ?: "")
            }
        }
    }

    private fun updateUserDataInDatabase(
        uid: String,
        name: String,
        username: String,
        phone: String,
        password: String
    ) {
        val userUpdates = mapOf(
            "name" to name,
            "username" to username,
            "phone" to phone,
            "password" to password
        )

        database.child("Users").child(uid).updateChildren(userUpdates)
    }

    private fun loadProfileImage() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val userRef = database.child("Users").child(uid)

        userRef.child("profileImage").get().addOnSuccessListener { snapshot ->
            val base64String = snapshot.getValue(String::class.java)
            if (!base64String.isNullOrEmpty()) {
                val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                accountIcon.setImageBitmap(bitmap)
            } else {
                accountIcon.setImageResource(R.drawable.img_placeholder_user)
            }
        }.addOnFailureListener {
            accountIcon.setImageResource(R.drawable.img_placeholder_user)
        }
    }

    private fun enableEditing(editText: EditText) {
        editText.isFocusableInTouchMode = true
        editText.isFocusable = true
        editText.isCursorVisible = true
        editText.requestFocus()

        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun validateFields(): Boolean {
        return if (
            etName.text.isNotEmpty() &&
            etUsername.text.isNotEmpty() &&
            etPhone.text.isNotEmpty() &&
            etPassword.text.isNotEmpty()
        ) {
            true
        } else {
            Toast.makeText(this, "All fields must be filled!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun saveChanges() {
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        val credential = EmailAuthProvider.getCredential(currentUser.email!!, etPassword.text.toString())

        currentUser.reauthenticate(credential)
            .addOnSuccessListener {
                val name = etName.text.toString()
                val username = etUsername.text.toString()
                val phone = etPhone.text.toString()
                val password = etPassword.text.toString()

                updateUserDataInDatabase(uid, name, username, phone, password)
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Authentication failed!", Toast.LENGTH_SHORT).show()
            }
    }

    private fun checkAndRequestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33) uses READ_MEDIA_IMAGES
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_IMAGES)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_MEDIA_IMAGES), 100)
            } else {
                openImagePicker()
            }
        } else {
            // Android 12 and below
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 100)
            } else {
                openImagePicker()
            }
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    private fun saveImageToDatabase(uri: Uri) {
        val uid = firebaseAuth.currentUser?.uid ?: return

        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
            val imageBytes = byteArrayOutputStream.toByteArray()
            val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)

            database.child("Users").child(uid).child("profileImage").setValue(base64Image)
                .addOnSuccessListener {
                    Toast.makeText(this, "Profile picture updated!", Toast.LENGTH_SHORT).show()
                    loadProfileImage()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save image!", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error processing image.", Toast.LENGTH_SHORT).show()
        }
    }
}
