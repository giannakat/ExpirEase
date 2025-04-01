package com.example.expirease.helper

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import com.example.expirease.R

class ItemDetailsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_item_details)

        val item_photo = findViewById<ImageView>(R.id.item_photo)
        val item_name = findViewById<TextView>(R.id.item_name)
        val item_quantity = findViewById<TextView>(R.id.item_quantity)

        intent?.let {
//            it?.getIntExtra("photo", R.drawable.avocado)?.let { photo ->
//                item_photo.setImageResource(photo)
//            }

            it?.getStringExtra("name")?.let { name ->
                item_name.setText(name)
            }

//            it?.getIntExtra("quantity", 0)?.let { quantity ->
//                item_quantity.text = "Quantity: $quantity"
//            }

        }

//        // Retrieve the data passed from the Intent
//        val photoResource = intent.getIntExtra("photo", R.drawable.avocado) // Default value for safety
//        val name = intent.getStringExtra("name") ?: "Unknown Item" // Default name if null
//        val quantity = intent.getIntExtra("quantity", 0) // Default value if quantity is not passed
//
//        // Set the retrieved values to the UI elements
//        item_photo.setImageResource(photoResource)
//        item_name.text = name
//        item_quantity.text = "Quantity: $quantity"

    }
}