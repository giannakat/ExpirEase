package com.example.expirease.helperNotif

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.expirease.R

class NotificationDetailsDialogFragment : DialogFragment() {

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(), // 85% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT // Height adjusts to content
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
        val itemQuantity = view.findViewById<TextView>(R.id.item_quantity)

        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.banana))
            itemName.text = it.getString("name") ?: "Unknown Item"
            itemQuantity.text = "Quantity: ${it.getInt("quantity", 0)}"
        }
    }
}