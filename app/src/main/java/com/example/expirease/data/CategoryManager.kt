package com.example.expirease.data

import android.graphics.Color.parseColor


object CategoryManager {
    private val categories = listOf(
        Category("dairy", "Dairy", parseColor("#ECEEFB")),
        Category("meat", "Meat",  parseColor("#FFEEE5")),
        Category("vegetables", "Vegetables",  parseColor("#E0F0FD")),
        Category("fruits", "Fruits",  parseColor("#DFFAD6")),
        Category("beverages", "Beverages",  parseColor("#FFF0CC")),
        Category("others", "Others",  parseColor("#FFEED6"))
        // Add more categories as needed
    )

    fun getCategories(): List<Category> = categories

}
