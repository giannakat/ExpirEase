package com.example.expirease.helper

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.expirease.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ItemDetailsDialogFragment : DialogFragment() {

    override fun onStart() {
        super.onStart()

        // Set custom width and height
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),  // 85% of screen width
            ViewGroup.LayoutParams.WRAP_CONTENT  // Height adjusts to content
        )
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_item_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val itemPhoto = view.findViewById<ImageView>(R.id.item_photo)
        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemQuantity = view.findViewById<TextView>(R.id.item_quantity)
        val itemExpiryDate = view.findViewById<TextView>(R.id.item_expiryDate)
        val itemCategory = view.findViewById<TextView>(R.id.item_category)

        // Retrieve data from arguments
        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.img_product_banana))
            itemName.text = it.getString("name", "Unknown Item")
            itemQuantity.text = "Quantity: ${it.getInt("quantity", 0)}"

            val expiryMillis = it.getLong("expiryDate", System.currentTimeMillis())  // Get expiry date
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiryMillis))
            itemExpiryDate.text = "Expiry Date: $formattedDate"

            itemCategory.text = "Category: ${it.getString("category", "Others")}"
        }
    }
}