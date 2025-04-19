package com.example.expirease


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.expirease.helperNotif.NotificationHelper
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "Refreshed token: $token")
        // Optionally save it to Firestore or Realtime DB
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("FCM", "Message received: ${remoteMessage.data}")

        // Check if userId and itemIndex are present in the data
        val userId = remoteMessage.data["userId"] ?: run {
            Log.d("FCM", "UserId not found in message data")
            return
        }
        val itemIndex = remoteMessage.data["itemIndex"] ?: "0"

        // Fetch the item details from Firebase Realtime Database
        val dbRef = FirebaseDatabase.getInstance().reference
        dbRef.child("Users").child(userId).child("items").child(itemIndex)
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    // Fetch item details from the snapshot
                    val itemName = snapshot.child("name").value?.toString() ?: "Unknown item"
                    val expiryDateMillis = snapshot.child("expiryDate").value?.toString()?.toLongOrNull()
                        ?: System.currentTimeMillis() // Fallback to current time if expiryDate is invalid

                    // Convert expiryDateMillis to a readable date format (e.g., "yyyy-MM-dd")
                    val expiryDate = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                        .format(java.util.Date(expiryDateMillis))

                    // Create and show the notification
                    NotificationHelper.createNotificationChannel(this)
                    NotificationHelper.showNotification(
                        context = this,
                        title = "Item Expiry Alert",
                        message = "Item: $itemName is expiring soon ($expiryDate)",
                        id = 1
                    )
                } else {
                    Log.d("FCM", "Item not found in database")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FCM", "Error fetching data from Firebase: ${exception.message}")
            }
    }

}

