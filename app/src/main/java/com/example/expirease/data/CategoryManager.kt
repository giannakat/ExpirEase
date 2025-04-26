package com.example.expirease.data

import com.example.expirease.R

object CategoryManager {
    private val categories = listOf(
        Category("dairy", "Dairy"),
        Category("meat", "Meat"),
        Category("vegetables", "Vegetables"),
        Category("fruits", "Fruits"),
        Category("beverages", "Beverages"),
        // Add more categories as needed
    )

    fun getCategories(): List<Category> = categories

    fun getCategoryById(id: String): Category? {
        return categories.find { it.id == id }
    }
}
