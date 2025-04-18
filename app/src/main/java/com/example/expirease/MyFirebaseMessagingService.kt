package com.example.expirease


import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.expirease.helperNotif.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Called when the app is in the foreground or background
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if the message contains data.
        if (remoteMessage.data.isNotEmpty()) {
            val itemName = remoteMessage.data["itemName"]
            val expiryDate = remoteMessage.data["expiryDate"]

            // Make sure the channel is created
            NotificationHelper.createNotificationChannel(this)

            // Use the helper to show the notification
            NotificationHelper.showNotification(
                context = this,
                title = "Item Expiry Alert",
                message = "Item: $itemName is expiring soon ($expiryDate)",
                id = 1
            )
        }
    }

    // Called when the Firebase token is refreshed
    override fun onNewToken(token: String) {
        Log.d("FCM", "Refreshed token: $token")
    }

    private fun showNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = 1

        val notificationChannelId = "expirease_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Expirease Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create and display the notification
        val notification = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(R.drawable.img_product_banana)  // Use your own icon
            .setAutoCancel(true)
            .build()

        notificationManager.notify(notificationId, notification)
    }
}
