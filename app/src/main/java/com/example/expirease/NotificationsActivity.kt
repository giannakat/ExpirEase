package com.example.expirease

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.data.Item
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter
import com.example.expirease.helperNotif.NotificationDetailsDialogFragment
import com.example.expirease.viewmodel.SharedItemViewModel

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

        // Set up RecyclerView for expiring soon items
        val recyclerView1 = findViewById<RecyclerView>(R.id.notif_recyclerview1)
        recyclerView1.layoutManager = LinearLayoutManager(this) // Vertical scrolling
        expiringSoonAdapter = NotificationRecyclerViewAdapter(expiringSoonList, onClick = { item ->
            val dialog = NotificationDetailsDialogFragment()
            dialog.setItemData(item) { itemToRemove ->
                val index = expiringSoonList.indexOf(itemToRemove)
                if (index != -1) {
                    expiringSoonList.removeAt(index)
                    expiringSoonAdapter.notifyItemRemoved(index)
                }
            }
            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
        })
        recyclerView1.adapter = expiringSoonAdapter

        // Set up RecyclerView for expired items
        val recyclerView2 = findViewById<RecyclerView>(R.id.notif_recyclerview2)
        recyclerView2.layoutManager = LinearLayoutManager(this) // Vertical scrolling
        expiredAdapter = NotificationRecyclerViewAdapter(expiredList, onClick = { item ->
            val dialog = NotificationDetailsDialogFragment()
            dialog.setItemData(item) { itemToRemove ->
                val index = expiredList.indexOf(itemToRemove)
                if (index != -1) {
                    expiredList.removeAt(index)
                    expiredAdapter.notifyItemRemoved(index)
                }
            }
            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
        })
        recyclerView2.adapter = expiredAdapter

        // Observe filtered items (which excludes dismissed notifications)
        viewModel.filteredItems.observe(this, Observer { itemList ->
            val now = System.currentTimeMillis()

            expiringSoonList.clear()
            expiredList.clear()

            // Filter items for expiring soon (next 7 days)
            expiringSoonList.addAll(itemList.filter {
                it.expiryDate in now..(now + 7 * 24 * 60 * 60 * 1000)
            })

            // Filter items for expired
            expiredList.addAll(itemList.filter { it.expiryDate < now })

            // Notify adapters about data changes
            expiringSoonAdapter.notifyDataSetChanged()
            expiredAdapter.notifyDataSetChanged()
        })
    }

    override fun onResume() {
        super.onResume()
        // Refresh to ensure dismissed notifications are excluded after dialog closes
        viewModel.refreshFilteredItems()
    }
}
