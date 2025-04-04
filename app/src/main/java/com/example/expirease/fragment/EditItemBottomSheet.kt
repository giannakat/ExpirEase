package com.example.expirease.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.example.expirease.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class EditItemBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_edit_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val itemPhoto = view.findViewById<ImageView>(R.id.item_photo)
        val itemName = view.findViewById<TextView>(R.id.item_name)
        val itemQuantity = view.findViewById<TextView>(R.id.item_quantity)
        val itemExpiryDate = view.findViewById<TextView>(R.id.item_expiryDate)
        val itemCategory = view.findViewById<TextView>(R.id.item_category)

        // Retrieve data from arguments
        arguments?.let {
            itemPhoto.setImageResource(it.getInt("photo", R.drawable.banana))
            itemName.text = it.getString("name", "Unknown Item")
            itemQuantity.text = "Quantity: ${it.getInt("quantity", 0)}"

            val expiryMillis = it.getLong("expiryDate", System.currentTimeMillis())  // Get expiry date
            val formattedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(expiryMillis))
            itemExpiryDate.text = "Expiry Date: $formattedDate"

            itemCategory.text = "Category: ${it.getString("category", "Others")}"
        }

//        saveBtn.setOnClickListener {
//            val updatedName = nameField.text.toString()
//            val updatedQty = quantityField.text.toString().toIntOrNull() ?: 1
//            // Return data via interface or shared ViewModel
//            dismiss()
//        }
    }

}