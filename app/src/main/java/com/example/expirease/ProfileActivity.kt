package com.example.expirease

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

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

    private lateinit var currentPassword: String

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database = FirebaseDatabase.getInstance().reference

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
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

        editPhotoLayout.setOnClickListener { checkAndRequestPermission() }

        editNameIcon.setOnClickListener { enableEditing(etName) }
        editUsernameIcon.setOnClickListener { enableEditing(etUsername) }
        editPhoneIcon.setOnClickListener { enableEditing(etPhone) }
        editPasswordIcon.setOnClickListener { enableEditing(etPassword) }

        currentPassword = etPassword.text.toString()

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

    private fun fetchUserDataFromFirebase() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val userRef = database.child("Users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                etName.setText(snapshot.child("name").getValue(String::class.java) ?: "")
                etUsername.setText(snapshot.child("username").getValue(String::class.java) ?: "")
                etPhone.setText(snapshot.child("phone").getValue(String::class.java) ?: "")
                etPassword.setText(snapshot.child("password").getValue(String::class.java) ?: "")
            } else {
                Log.w("FETCH_USER", "No user data found.")
            }
        }.addOnFailureListener { e ->
            Log.e("FETCH_USER", "Failed to retrieve user data", e)
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
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DB_UPDATE", "Failed to update user data", e)
                Toast.makeText(this, "Failed to update data.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage() {
        val uid = firebaseAuth.currentUser?.uid
        if (uid != null) {
            val userRef = database.child("Users").child(uid)

            userRef.child("profileImage").get().addOnSuccessListener { snapshot ->
                val base64String = snapshot.getValue(String::class.java)
                if (base64String != null) {
                    val bitmap = decodeBase64ToBitmap(base64String)
                    accountIcon.setImageBitmap(bitmap)
                } else {
                    accountIcon.setImageResource(R.drawable.img_placeholder_user)
                }
            }.addOnFailureListener { e ->
                Log.e("LOAD_IMAGE", "Failed to load profile image", e)
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
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val phone = etPhone.text.toString()
        val password = etPassword.text.toString()

        updateUserDataInDatabase(uid, name, username, phone, password)
    }

    private fun checkAndRequestPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            openImagePicker()
        } else {
            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    private fun saveImageUri(uri: String) {
        with(sharedPrefs.edit()) {
            putString("imageUri", uri)
            apply()
        }
    }

    private fun saveImageToLocal(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.flush()
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving image locally.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadImageToFirebase(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)

            // Convert the Bitmap to a Base64 String
            val base64String = encodeImageToBase64(bitmap)

            // Save the Base64 String to Firebase Realtime Database
            val uid = firebaseAuth.currentUser?.uid
            if (uid != null) {
                val userRef = database.child("Users").child(uid)
                userRef.child("profileImage").setValue(base64String)
                    .addOnSuccessListener {
                        Log.d("UPLOAD", "Image saved successfully to Realtime Database")
                    }
                    .addOnFailureListener { e ->
                        Log.e("UPLOAD", "Failed to save image", e)
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error uploading image to Firebase.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun decodeBase64ToBitmap(base64String: String): Bitmap {
        val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }
}
