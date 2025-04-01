package com.example.expirease.data

import com.example.expirease.R

data class Item(
    var name: String = "",
    var quantity: Int = 0,
    var expiryDate: Long,
    var photoResource: Int = R.drawable.banana
)
