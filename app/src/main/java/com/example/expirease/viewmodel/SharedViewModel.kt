package com.example.expirease.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expirease.data.Item
import java.util.Date

class SharedViewModel : ViewModel() {
    // This will store all items
    val listOfItems = MutableLiveData<MutableList<Item>>()

    init {
        listOfItems.value = mutableListOf()
    }

    // Add an item to the list
    fun addItem(item: Item) {
        val currentList = listOfItems.value ?: mutableListOf()
        currentList.add(item)
        listOfItems.value = currentList
    }

    // Update the entire list
    fun updateItems(items: MutableList<Item>) {
        listOfItems.value = items
    }

    // Get expired items
    fun getExpiredItems(): List<Item> {
        return listOfItems.value?.filter { isExpired(it.expiryDate) } ?: emptyList()
    }

    // Get soon-to-expire items
    fun getExpiringSoonItems(): List<Item> {
        return listOfItems.value?.filter { isExpiringSoon(it.expiryDate) } ?: emptyList()
    }

    private fun isExpired(expiryDate: Long): Boolean {
        val today = System.currentTimeMillis()
        return expiryDate < today
    }

    private fun isExpiringSoon(expiryDate: Long): Boolean {
        val today = System.currentTimeMillis()
        val threeDaysLater = today + (3*24*60*60*1000)
        return expiryDate in today..threeDaysLater
    }
}