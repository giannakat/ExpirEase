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
import com.google.firebase.auth.EmailAuthProvider
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

    // Register for image picking and permissions
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            try {
                // Ensure persistable URI permission
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                Log.d("PICK_IMAGE", "Picked image URI: $it")

                // Save URI for future use
                saveImageUri(it.toString())

                // Save the image locally (optional, for caching)
                saveImageToLocal(it)

                // Upload image to Firebase
                uploadImageToFirebase(it)

                // Optionally, load the profile image
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

        // Initialize your views here
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

        // Check email verification status
        checkEmailVerification()

        fetchUserDataFromFirebase()
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

    // Check if the email is verified
    private fun checkEmailVerification() {
        firebaseAuth.currentUser?.reload()?.addOnCompleteListener { reloadTask ->
            if (reloadTask.isSuccessful) {
                val refreshedUser = firebaseAuth.currentUser
                Log.d("EMAIL_VERIFIED", "Email verified? ${refreshedUser?.isEmailVerified}")
                if (refreshedUser?.isEmailVerified == false) {
                    Toast.makeText(this, "Please verify your email to continue.", Toast.LENGTH_LONG).show()
                    firebaseAuth.signOut()
                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()
                } else {
                    Log.d("EMAIL_VERIFIED", "Access granted to verified user")
                }
            } else {
                Log.e("EMAIL_VERIFIED", "Failed to reload user: ${reloadTask.exception?.message}")
            }
        }

    }

    private fun fetchUserDataFromFirebase() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val userRef = database.child("Users").child(uid)

        userRef.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                val name = snapshot.child("name").getValue(String::class.java)
                val username = snapshot.child("username").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)
                val phone = snapshot.child("phone").getValue(String::class.java)
                val password = snapshot.child("password").getValue(String::class.java)

                etName.setText(name ?: "")
                etUsername.setText(username ?: "")
                etEmail.setText(email ?: "")
                etPhone.setText(phone ?: "")
                etPassword.setText(password ?: "")
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
        email: String,
        phone: String,
        password: String
    ) {
        val userUpdates = mapOf(
            "name" to name,
            "username" to username,
            "email" to email,
            "phone" to phone,
            "password" to password
        )

        database.child("Users").child(uid).updateChildren(userUpdates)
            .addOnSuccessListener {
                Log.d("DB_UPDATE", "User data updated in Realtime Database")
                Toast.makeText(this, "Profile updated successfully!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Log.e("DB_UPDATE", "Failed to update user data", e)
                Toast.makeText(this, "Failed to update data in database.", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadProfileImage() {
        val uriString = sharedPrefs.getString("imageUri", null)

        // If there is no URI stored or URI permission revoked
        if (uriString == null) {
            accountIcon.setImageResource(R.drawable.img_placeholder_user)
            return
        }

        val uri = Uri.parse(uriString)

        // Check if URI permission is still valid
        val hasPermission = contentResolver.persistedUriPermissions.any {
            it.uri == uri && it.isReadPermission
        }

        if (hasPermission) {
            accountIcon.setImageURI(uri)
        } else {
            // Permission was revoked, show placeholder
            Log.e("PROFILE_IMAGE", "Image access revoked or expired.")
            accountIcon.setImageResource(R.drawable.img_placeholder_user)
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
        val currentUser = firebaseAuth.currentUser
        val uid = currentUser?.uid

        if (uid == null || currentUser.email == null) {
            Toast.makeText(this, "User not authenticated!", Toast.LENGTH_SHORT).show()
            return
        }

        val newEmail = etEmail.text.toString()
        val password = etPassword.text.toString()
        val name = etName.text.toString()
        val username = etUsername.text.toString()
        val phone = etPhone.text.toString()

        // Re-authenticate the user with their current credentials
        val credential = EmailAuthProvider.getCredential(currentUser.email!!, password)
        currentUser.reauthenticate(credential)
            .addOnCompleteListener { reauthTask ->
                if (reauthTask.isSuccessful) {
                    Log.d("AUTH", "Re-authentication successful")

                    // Update email if changed
                    if (newEmail != currentUser.email) {
                        currentUser.verifyBeforeUpdateEmail(newEmail)
                            .addOnCompleteListener { emailTask ->
                                if (emailTask.isSuccessful) {
                                    Log.d("EMAIL_UPDATE", "Verification email sent for new email")

                                    Toast.makeText(
                                        this,
                                        "Please verify your new email address. Changes will apply after verification.",
                                        Toast.LENGTH_LONG
                                    ).show()

                                    // Save the rest of the data in Realtime Database
                                    updateUserDataInDatabase(uid, name, username, newEmail, phone, password)

                                } else {
                                    val errorMsg = emailTask.exception?.message
                                    Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_SHORT).show()
                                    Log.e("EMAIL_UPDATE", "Email update failed: $errorMsg", emailTask.exception)
                                }
                            }
                    } else {
                        // No email change, proceed to update other details
                        updateUserDataInDatabase(uid, name, username, newEmail, phone, password)
                    }
                } else {
                    Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show()
                }
            }
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
        val storageRef = storage.reference.child("profile_images/${firebaseAuth.currentUser?.uid}")
        val uploadTask = storageRef.putFile(uri)

        uploadTask.addOnSuccessListener {
            storageRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                Log.d("UPLOAD", "Image uploaded successfully: $downloadUrl")
            }
        }.addOnFailureListener { e ->
            Log.e("UPLOAD", "Failed to upload image", e)
        }
    }

}
