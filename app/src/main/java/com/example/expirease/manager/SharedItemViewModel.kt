package com.example.expirease.manager

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expirease.app.MyApplication
import com.example.expirease.data.Category
import com.example.expirease.data.HistoryItem
import com.example.expirease.data.Item
import com.example.expirease.data.ItemStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SharedItemViewModel : ViewModel() {

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    //TODO connect to the user
   // private val app = application as MyApplication


    // Initialize the list
    init {
       _allItems.value = getAllItems()// Assuming `allItems` includes both shared and non-shared
   }

    private fun getAllItems(): List<Item> {
        return listOf() // start empty
    }

    fun getItemsByStatus(status: ItemStatus): LiveData<List<Item>> {
        val filteredItems = _allItems.value?.filter { it.status == status } ?: listOf()
        val liveData = MutableLiveData<List<Item>>()
        liveData.value = filteredItems
        return liveData
    }

    // Function to filter items based on their status
    fun getFilteredItems(status: ItemStatus): LiveData<List<Item>> {
        val filteredList = _allItems.value?.filter { it.status == status } ?: emptyList()
        val liveData = MutableLiveData<List<Item>>()
        liveData.value = filteredList
        return liveData
    }

    // Function to update item status (e.g., marking an item as expired)
    fun updateItem(updated: Item) {
        _allItems.value = _allItems.value?.map {
            if (it.name == updated.name && it.expiryDate == updated.expiryDate) updated else it
        }
    }

    fun getItemsByCategory(category: Category): LiveData<List<Item>> {
        val filteredItems = _allItems.value?.filter { it.category == category } ?: listOf()
        val liveData = MutableLiveData<List<Item>>()
        liveData.value = filteredItems
        return liveData
    }

    fun getItemsForDate(date: LocalDate): List<Item> {
        return _allItems.value?.filter {
            Instant.ofEpochMilli(it.expiryDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        } ?: emptyList()
    }

    fun getFreshItems(): List<Item> {
        return _allItems.value?.filter {
            LocalDate.ofEpochDay(it.expiryDate).isAfter(LocalDate.now().plusDays(3))
        } ?: emptyList()
    }

    fun getExpiringSoon(): List<Item> {
        return _allItems.value?.filter {
            val expiry = LocalDate.ofEpochDay(it.expiryDate)
            expiry.isAfter(LocalDate.now()) && expiry <= LocalDate.now().plusDays(3)
        } ?: emptyList()
    }

    fun getExpiredItems(): List<Item> {
        return _allItems.value?.filter {
            LocalDate.ofEpochDay(it.expiryDate).isBefore(LocalDate.now())
        } ?: emptyList()
    }

    fun addItem(item : Item){
        val currentList = _allItems.value?.toMutableList() ?: mutableListOf()
        currentList.add(item)
        _allItems.value = currentList
    }

    fun restoreItem(item: Item) {
        item.status = ItemStatus.ACTIVE
        updateItem(item) // Assuming this will update LiveData and notify observers
    }


    // Simulated function — replace with actual logic if needed



//    val items: LiveData<List<Item>>  get() = _items

//    fun setItems(newItems: List<Item>) {
//        _items.value = newItems
//    }
//
//
//
//    fun getItemsForDate(date: LocalDate): List<Item> {
//        return _items.value?.filter {
//            LocalDate.ofEpochDay(it.expiryDate) == date
//        } ?: emptyList()
//    }
//
//    fun getFreshItems(): List<Item> = _items.value?.filter {
//        LocalDate.ofEpochDay(it.expiryDate).isAfter(LocalDate.now().plusDays(3))
//    } ?: emptyList()
//
//    fun getExpiringSoon(): List<Item> = _items.value?.filter {
//        val expiry = LocalDate.ofEpochDay(it.expiryDate)
//        expiry.isAfter(LocalDate.now()) && expiry <= LocalDate.now().plusDays(3)
//    } ?: emptyList()
//
//    fun getExpiredItems(): List<Item> = _items.value?.filter {
//        LocalDate.ofEpochDay(it.expiryDate).isBefore(LocalDate.now())
//    } ?: emptyList()

//    fun getExpiredItems(): LiveData<List<Item>> {
//        return getItemsByStatus(ItemStatus.EXPIRED)
//    }
//    fun addToHistory(item: HistoryItem) {
//        val currentList = _historyItems.value?.toMutableList() ?: mutableListOf()
//        currentList.add(0, item) // Add to top of history
//        _historyItems.value = currentList.toList() // Convert to List<HistoryItem>
//    }


}