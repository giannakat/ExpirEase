package com.example.expirease.data

import com.example.expirease.R

enum class ItemStatus {
    ACTIVE,
    CONSUMED,
    EXPIRED,
    DELETED
}

data class Item(
    var name: String = "",
    var quantity: Int = 0,
    var expiryDate: Long,
    var category: Category,
    var photoResource: Int = R.drawable.img_default_product,
    var status :ItemStatus = ItemStatus.ACTIVE
)
