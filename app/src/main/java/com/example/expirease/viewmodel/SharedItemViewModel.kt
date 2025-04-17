package com.example.expirease.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.data.ItemStatus
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class SharedItemViewModel : ViewModel() {

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    //TODO connect to the user
   // private val app = application as MyApplication

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val database: DatabaseReference
        get() = FirebaseDatabase.getInstance().getReference("Users/${firebaseAuth.currentUser?.uid}/items")

    init {
        fetchItemsFromFirebase()
    }

    private fun fetchItemsFromFirebase() {
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<Item>()
                snapshot.children.forEach { itemSnapshot ->
                    itemSnapshot.getValue(Item::class.java)?.let {
                        items.add(it)
                    }
                }
                _allItems.value = items
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error if needed
            }
        })
    }

    fun addItem(item : Item){
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(item)
        }

        val currentList = _allItems.value?.toMutableList() ?: mutableListOf()
        currentList.add(item)
        _allItems.value = currentList
        item.category.incrementItemCount()
    }

    fun updateItem(updated: Item) {
        val snapshotRef = database.orderByChild("expiryDate").equalTo(updated.expiryDate.toDouble())
        snapshotRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    val existingItem = child.getValue(Item::class.java)
                    if (existingItem?.name == updated.name) {
                        child.ref.setValue(updated)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        _allItems.value = _allItems.value?.map {
            if (it.name == updated.name && it.expiryDate == updated.expiryDate) updated else it
        }
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



    fun restoreItem(item: Item) {
        item.status = ItemStatus.ACTIVE
        updateItem(item) // Assuming this will update LiveData and notify observers
    }

    fun saveItemsToFirebase(onComplete: () -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser
        val items = _allItems.value ?: emptyList()

        if (user != null) {
            val uid = user.uid
            val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$uid/items")

            databaseRef.setValue(items)
                    .addOnSuccessListener {
                        Log.d("Firebase", "Items saved successfully")
                        onComplete()
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Failed to save items", e)
                        onComplete()
                    }
        } else {
            Log.w("Firebase", "User is null during saveItemsToFirebase")
            onComplete()
        }

//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            val uid = user.uid
//            val databaseRef = FirebaseDatabase.getInstance().getReference("Users/$uid/items")
//            val itemsMap = _allItems.value { it.toMap() }
//
//            databaseRef.setValue(itemsMap)
//                .addOnSuccessListener {
//                    Log.d("Firebase", "Items saved successfully")
//                    onComplete()
//                }
//                .addOnFailureListener { e ->
//                    Log.e("Firebase", "Failed to save items", e)
//                    onComplete()
//                }
//        } else {
//            Log.w("Firebase", "User is null during saveItemsToFirebase")
//            onComplete()
//        }
    }

}