package com.example.expirease.data


object CategoryManager {
    private val categories = listOf(
        Category("dairy", "Dairy"),
        Category("meat", "Meat"),
        Category("vegetables", "Vegetables"),
        Category("fruits", "Fruits"),
        Category("beverages", "Beverages"),
        Category("others", "Others")
        // Add more categories as needed
    )

    fun getCategories(): List<Category> = categories

}
