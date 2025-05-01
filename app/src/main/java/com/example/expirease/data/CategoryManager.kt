package com.example.expirease.data

import android.graphics.Color


object CategoryManager {
    private val categories = listOf(
        Category("dairy", "Dairy", Color.parseColor("#FFCDD2")),
        Category("meat", "Meat",  Color.parseColor("#D7CCC8")),
        Category("vegetables", "Vegetables",  Color.parseColor("#D7CCC8")),
        Category("fruits", "Fruits",  Color.parseColor("#D7CCC8")),
        Category("beverages", "Beverages",  Color.parseColor("#D7CCC8")),
        Category("others", "Others",  Color.parseColor("#D7CCC8"))
        // Add more categories as needed
    )

    fun getCategories(): List<Category> = categories

}
