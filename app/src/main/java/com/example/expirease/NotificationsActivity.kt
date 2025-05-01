package com.example.expirease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
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

    private lateinit var expiringSoonAdapter: NotificationRecyclerViewAdapter
    private lateinit var expiredAdapter: NotificationRecyclerViewAdapter

    private var expiringSoonList: MutableList<Item> = mutableListOf()
    private var expiredList: MutableList<Item> = mutableListOf()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val backButton: ImageView = findViewById(R.id.back_button)
        backButton.setOnClickListener {
            val intent = Intent(this, HomeWithFragmentActivity::class.java)
            startActivity(intent)
        }

        val recyclerView1 = findViewById<RecyclerView>(R.id.notif_recyclerview1)
        recyclerView1.layoutManager = LinearLayoutManager(this)
        expiringSoonAdapter = NotificationRecyclerViewAdapter(expiringSoonList, onClick = {})
        recyclerView1.adapter = expiringSoonAdapter

        val recyclerView2 = findViewById<RecyclerView>(R.id.notif_recyclerview2)
        recyclerView2.layoutManager = LinearLayoutManager(this)
        expiredAdapter = NotificationRecyclerViewAdapter(expiredList, onClick = {})
        recyclerView2.adapter = expiredAdapter

        val swipeToDeleteCallback1 = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = expiringSoonList[viewHolder.adapterPosition]
                deleteItemFromFirebase(item)
            }
        }

        val swipeToDeleteCallback2 = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(rv: RecyclerView, vh: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder) = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val item = expiredList[viewHolder.adapterPosition]
                deleteItemFromFirebase(item)
            }
        }

        ItemTouchHelper(swipeToDeleteCallback1).attachToRecyclerView(recyclerView1)
        ItemTouchHelper(swipeToDeleteCallback2).attachToRecyclerView(recyclerView2)

        viewModel.filteredItems.observe(this, Observer { itemList ->
            val now = System.currentTimeMillis()

            expiringSoonList.clear()
            expiredList.clear()

            if (itemList != null) {
                expiringSoonList.addAll(itemList.filter {
                    it.expiryDate in now..(now + 7 * 24 * 60 * 60 * 1000)
                })
            }

            if (itemList != null) {
                expiredList.addAll(itemList.filter { it.expiryDate < now })
            }

            expiringSoonAdapter.notifyDataSetChanged()
            expiredAdapter.notifyDataSetChanged()
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
