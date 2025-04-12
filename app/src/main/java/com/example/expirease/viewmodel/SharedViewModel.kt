package com.example.expirease.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expirease.data.Item

class SharedViewModel (application: Application) : AndroidViewModel(application) {
    // This will hold the list of all items
    val items = MutableLiveData<MutableList<Item>>(mutableListOf())

    // Method to add an item to the list
    fun addItem(item: Item) {
        val updatedList = items.value ?: mutableListOf()
        updatedList.add(item)
        items.value = updatedList
    }

    // You can add other methods to remove or update items as needed
}
