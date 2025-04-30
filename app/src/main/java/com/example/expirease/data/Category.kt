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

    override fun toString(): String {
        return displayName
    }

}
