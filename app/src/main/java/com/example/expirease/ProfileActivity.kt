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

    private var selectedImageUri: Uri? = null

    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                selectedImageUri = it
                saveImageUri(it.toString())
                saveImageToLocal(it)
                loadProfileImage()
            } catch (e: SecurityException) {
                e.printStackTrace()
                Toast.makeText(this, "Permission denied for image!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) openImagePicker()
        else Toast.makeText(this, "Storage permission denied.", Toast.LENGTH_SHORT).show()
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

        editPhotoLayout.setOnClickListener { checkAndRequestPermission() }

        editNameIcon.setOnClickListener { enableEditing(etName) }
        editUsernameIcon.setOnClickListener { enableEditing(etUsername) }
        editEmailIcon.setOnClickListener { enableEditing(etEmail) }
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

        if (uriString != null) {
            val uri = Uri.parse(uriString)
            val hasPermission = contentResolver.persistedUriPermissions.any {
                it.uri == uri && it.isReadPermission
            }
            if (hasPermission) {
                accountIcon.setImageURI(uri)
                return
            }
        }

        if (file.exists()) {
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            accountIcon.setImageBitmap(bitmap)
            return
        }

        // ✅ Fetch from Firebase Storage if not found locally
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        if (userId != null) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_images/$userId.jpg")
            val localFile = File.createTempFile("temp_profile", ".jpg")

            storageRef.getFile(localFile).addOnSuccessListener {
                val bitmap = BitmapFactory.decodeFile(localFile.absolutePath)
                accountIcon.setImageBitmap(bitmap)
            }.addOnFailureListener {
                accountIcon.setImageResource(R.drawable.img_placeholder_user)
            }
        } else {
            accountIcon.setImageResource(R.drawable.img_placeholder_user)
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
        ) true else {
            Toast.makeText(this, "All fields must be filled before saving!", Toast.LENGTH_SHORT).show()
            false
        }
    }

    private fun saveChanges() {
        val app = application as MyApplication
        val uid = FirebaseAuth.getInstance().currentUser?.uid

        if (uid == null) {
            Toast.makeText(this, "No authenticated user!", Toast.LENGTH_SHORT).show()
            return
        }

        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val phone = etPhone.text.toString()
        val password = etPassword.text.toString()

        // Save locally
        app.username = username
        app.password = password
        app.email = email
        app.name = name
        sharedPrefs.edit().putString("phone", phone).apply()

        // Upload profile image to Firebase Storage if new image selected
        if (selectedImageUri != null) {
            val storageRef = FirebaseStorage.getInstance().reference
                .child("profile_images/$uid.jpg")

            storageRef.putFile(selectedImageUri!!)
                .addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                        saveUserDataToFirebase(uid, name, username, email, phone, password, downloadUri.toString())
                    }.addOnFailureListener { uriError ->
                        Log.e("ProfileUpload", "Download URL failed: ${uriError.message}")
                        Toast.makeText(this, "Failed to get download URL", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { uploadError ->
                    Log.e("ProfileUpload", "Upload failed: ${uploadError.message}")
                    Toast.makeText(this, "Image upload failed!", Toast.LENGTH_SHORT).show()
                }
        } else {
            // No new image, use existing URI if any
            val existingImageUri = sharedPrefs.getString("imageUri", null)
            saveUserDataToFirebase(uid, name, username, email, phone, password, existingImageUri)
        }
    }

    private fun saveUserDataToFirebase(
        uid: String,
        name: String,
        username: String,
        email: String,
        phone: String,
        password: String,
        imageUrl: String?
    ) {
        val dbRef = FirebaseDatabase.getInstance().getReference("Users").child(uid)
        val userMap = mapOf(
            "name" to name,
            "username" to username,
            "email" to email,
            "phone" to phone,
            "password" to password,
            "profileImageUrl" to imageUrl
        )

        dbRef.updateChildren(userMap).addOnSuccessListener {
            Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to update profile!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveImageUri(uri: String) {
        sharedPrefs.edit().putString("imageUri", uri).apply()
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            Manifest.permission.READ_MEDIA_IMAGES
        else
            Manifest.permission.READ_EXTERNAL_STORAGE

        if (ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED) {
            openImagePicker()
        } else {
            permissionLauncher.launch(permission)
        }
    }

    private fun openImagePicker() {
        pickImageLauncher.launch(arrayOf("image/*"))
    }

    private fun saveImageToLocal(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            val file = File(filesDir, "profile_image.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()

            Log.d("Profile", "Image saved locally to ${file.absolutePath}")
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image locally.", Toast.LENGTH_SHORT).show()
        }
    }
}
