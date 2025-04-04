package com.example.expirease.data

enum class Category(val displayName: String) {
    DAIRY("Dairy"),
    MEAT("Meat"),
    VEGETABLES("Vegetables"),
    FRUITS("Fruits"),
    BAKERY("Bakery"),
    OTHER("Other");

    var itemCount: Int = 0
        private set

    fun incrementItemCount() {
        itemCount++
    }

    fun resetItemCount() {
        itemCount = 0
    }

    override fun toString(): String {
        return displayName
    }
}