package com.example.expirease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.data.Item
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter
import com.example.expirease.viewmodel.SharedItemViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class NotificationsActivity : AppCompatActivity() {
    private val viewModel: SharedItemViewModel by viewModels()

    private lateinit var adapter: NotificationRecyclerViewAdapter
    private val combinedList: MutableList<Any> = mutableListOf() // Class-level variable

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeWithFragmentActivity::class.java)
            startActivity(intent)
        }

        // Initialize RecyclerView and Adapter
        val recyclerView = findViewById<RecyclerView>(R.id.notif_recyclerview1)
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = NotificationRecyclerViewAdapter(combinedList) { /* onClick */ }
        recyclerView.adapter = adapter

        // Swipe-to-delete logic
        val swipeToDeleteCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val swipedItem = combinedList[viewHolder.adapterPosition]
                if (swipedItem is Item) {
                    deleteItemFromFirebase(swipedItem)
                    combinedList.removeAt(viewHolder.adapterPosition)
                    adapter.notifyItemRemoved(viewHolder.adapterPosition)
                } else {
                    // Don't allow swipe for headers
                    adapter.notifyItemChanged(viewHolder.adapterPosition)
                }
            }
        }
        ItemTouchHelper(swipeToDeleteCallback).attachToRecyclerView(recyclerView)

        // Observe filtered items from ViewModel
        viewModel.filteredItems.observe(this, Observer { itemList ->
            val now = System.currentTimeMillis()

            val expiringSoon = itemList?.filter {
                it.expiryDate in now..(now + 3 * 24 * 60 * 60 * 1000)
            } ?: emptyList()

            val expired = itemList?.filter {
                it.expiryDate < now
            } ?: emptyList()

            // Clear previous list and update with headers and filtered items
            combinedList.clear()

            // Add "Expiring Soon" header and items
            if (expiringSoon.isNotEmpty()) {
                combinedList.add("Expiring Soon") // Add header
                combinedList.addAll(expiringSoon) // Add items
            }

            // Add "Expired" header and items
            if (expired.isNotEmpty()) {
                combinedList.add("Expired") // Add header
                combinedList.addAll(expired) // Add items
            }

            // Control visibility of header TextViews based on lists

            // Notify adapter of data changes
            adapter.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshFilteredItems()
    }

    private fun deleteItemFromFirebase(item: Item) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val itemId = item.name + "_" + item.expiryDate
        if (userId != null) {
            val ref = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(userId)
                .child("dismissedNotifications")
                .child(itemId)
            ref.setValue(true)
        }
        viewModel.dismissItem(item)
    }
}
