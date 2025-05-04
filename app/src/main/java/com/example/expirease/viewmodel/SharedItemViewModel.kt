package com.example.expirease.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.expirease.data.CategoryManager
import com.example.expirease.data.Item
import com.example.expirease.data.ItemStatus
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class SharedItemViewModel : ViewModel() {

    private val _allItems = MutableLiveData<List<Item>>()
    val allItems: LiveData<List<Item>> get() = _allItems

    private val _filteredItems = MutableLiveData<List<Item>?>()
    val filteredItems: MutableLiveData<List<Item>?> get() = _filteredItems

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val userId = firebaseAuth.currentUser?.uid
    private val database: DatabaseReference
        get() = FirebaseDatabase.getInstance().getReference("Users/$userId/items")
    private val dismissedRef: DatabaseReference
        get() = FirebaseDatabase.getInstance().getReference("Users/$userId/dismissedNotifications")

    init {
        fetchItemsWithDismissFilter()
    }

    private fun fetchItemsWithDismissFilter() {
        if (userId == null) return

        dismissedRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dismissedSnapshot: DataSnapshot) {
                val dismissedSet = dismissedSnapshot.children.mapNotNull { it.key }.toSet()

                database.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val items = mutableListOf<Item>()
                        val filtered = mutableListOf<Item>()

                        for (itemSnap in snapshot.children) {
                            val item = itemSnap.getValue(Item::class.java)
                            if (item != null) {
                                items.add(item)

                                val itemId = "${item.name}_${item.expiryDate}"
                                if (!dismissedSet.contains(itemId)) {
                                    filtered.add(item)
                                }
                            }
                        }

                        _allItems.value = items
                        _filteredItems.value = filtered
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("ViewModel", "Failed to load items: ${error.message}")
                    }
                })
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ViewModel", "Failed to load dismissed notifications: ${error.message}")
            }
        })
    }

    fun refreshFilteredItems() {
        fetchItemsWithDismissFilter()
    }

    fun addItem(item: Item) {
        val key = database.push().key
        if (key != null) {
            database.child(key).setValue(item)
        }

        val currentList = _allItems.value?.toMutableList() ?: mutableListOf()
        currentList.add(item)
        _allItems.value = currentList

        val category = CategoryManager.getCategories().find { it.id == item.categoryId }
        category?.incrementItemCount()
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

    fun getItemsForDate(date: LocalDate): List<Item> {
        return _allItems.value?.filter {
            Instant.ofEpochMilli(it.expiryDate)
                .atZone(ZoneId.systemDefault())
                .toLocalDate() == date
        } ?: emptyList()
    }

    fun restoreItem(item: Item) {
        item.status = ItemStatus.ACTIVE
        updateItem(item)
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
    }

    fun updateItemInViewModel(updated: Item) {
        _allItems.value = _allItems.value?.map {
            if (it.name == updated.name && it.expiryDate == updated.expiryDate) updated else it
        }
    }

    fun dismissItem(item: Item) {
        val user = FirebaseAuth.getInstance().currentUser
        val itemId = "${item.name}_${item.expiryDate}"

        if (user != null) {
            val uid = user.uid
            val ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("dismissedNotifications")
                .child(itemId)

            ref.setValue(true).addOnSuccessListener {
                val updatedList = _filteredItems.value?.toMutableList()
                updatedList?.removeIf {
                    it.name == item.name && it.expiryDate == item.expiryDate
                }
                _filteredItems.value = updatedList
            }.addOnFailureListener {
                Log.e("ViewModel", "Failed to dismiss item: ${it.message}")
            }
        }
    }
}
