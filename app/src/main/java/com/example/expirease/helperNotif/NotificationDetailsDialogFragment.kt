package com.example.expirease.helperNotif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.expirease.R
import com.example.expirease.data.Item
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationDetailsDialogFragment : DialogFragment() {

    private var item: Item? = null
    private var onRemove: ((Item) -> Unit)? = null

    fun setItemData(item: Item, onRemove: (Item) -> Unit) {
        this.item = item
        this.onRemove = onRemove
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_notification_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemPhoto = view.findViewById<ImageView>(R.id.item_photo)
        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemExpiry = view.findViewById<TextView>(R.id.item_expiryDate)
        val removeButton = view.findViewById<Button>(R.id.remove_notification_button)

        item?.let {
            itemPhoto.setImageResource(it.photoResource)
            itemName.text = it.name
            itemExpiry.text = "Expiry: ${it.expiryDateString}" // ✅ Formatted date

            removeButton.setOnClickListener {
                item?.let {
                    val userId = FirebaseAuth.getInstance().currentUser?.uid
                    val itemId = it.name + "_" + it.expiryDate // Or any unique ID logic
                    if (userId != null) {
                        val ref = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(userId)
                            .child("dismissedNotifications")
                            .child(itemId)
                        ref.setValue(true)
                    }
                    onRemove?.invoke(it)
                    dismiss()
                }
            }

        }
    }
}
