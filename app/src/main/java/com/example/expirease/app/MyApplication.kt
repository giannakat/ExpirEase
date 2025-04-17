package com.example.expirease.app

import android.app.Application
import android.util.Log
import com.example.expirease.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MyApplication : Application() {

    var username: String = ""
    var password: String = ""
    var email: String = ""
    var name: String = "Gianna Carreon"

    var listOfItems: MutableList<Item> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        Log.d("Test", "MyApplication is initialized")
    }

    fun saveItemsToFirebase(onComplete: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            val uid = user.uid
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$uid/items")
            val itemsMap = listOfItems.map { it.toMap() }

            databaseRef.setValue(itemsMap)
                .addOnSuccessListener {
                    Log.d("Firebase", "Items saved successfully")
                    onComplete()
                }
                .addOnFailureListener { e ->
                    Log.e("Firebase", "Failed to save items", e)
                    onComplete()
                }
        } else {
            Log.w("Firebase", "User is null during saveItemsToFirebase")
            onComplete()
        }
    }
}
