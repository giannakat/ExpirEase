package com.example.expirease.helper

interface OnItemUpdatedListener {
    fun onItemUpdated(name: String, quantity: Int, expiryDate: Long, category: String)
}