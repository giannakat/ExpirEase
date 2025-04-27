package com.example.expirease.data

data class Category(
    val id: String,
    val displayName: String,
    var itemCount: Int = 0  // Add item count property
) {
    // Method to increment item count
    fun incrementItemCount() {
        itemCount++
    }

    // Optionally, reset the count (in case you need to reset it later)
    fun resetItemCount() {
        itemCount = 0
    }

    override fun toString(): String {
        return displayName
    }

    companion object {
        fun defaultCategories(): List<Category> {
            return listOf(
                Category(id = "dairy", displayName = "Dairy"),
                Category(id = "meat", displayName = "Meat"),
                Category(id = "vegetables", displayName = "Vegetables"),
                Category(id = "fruits", displayName = "Fruits"),
                Category(id = "bakery", displayName = "Bakery")
            )
        }
    }
}
