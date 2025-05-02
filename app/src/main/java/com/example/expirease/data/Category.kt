package com.example.expirease.data

import android.graphics.Color

data class Category(
    val id: String,
    val displayName: String,
    val backgroundColor: Int,
    var itemCount: Int = 0,  // Add item count property
) {
    // Method to increment item count
    fun incrementItemCount() {
        itemCount++
    }

    override fun toString(): String {
        return displayName
    }

}
