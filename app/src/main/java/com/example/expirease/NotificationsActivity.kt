package com.example.expirease

import SharedViewModel
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.data.Item
import com.example.expirease.helperNotif.NotificationDetailsDialogFragment
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter
import java.util.*

class NotificationsActivity : AppCompatActivity() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var allItems: MutableList<Item>
    private lateinit var expiringSoonList: MutableList<Item>
    private lateinit var expiredList: MutableList<Item>

    private lateinit var expiringSoonAdapter: NotificationRecyclerViewAdapter
    private lateinit var expiredAdapter: NotificationRecyclerViewAdapter

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        sharedViewModel = ViewModelProvider(this)[SharedViewModel::class.java]
        allItems = sharedViewModel.listOfItems


        // ✅ Split to expiring vs expired
        val now = System.currentTimeMillis()
        expiringSoonList = allItems.filter { it.expiryDate >= now }.toMutableList()
        expiredList = allItems.filter { it.expiryDate < now }.toMutableList()

        // ✅ RecyclerView for Expiring Soon
        val recyclerView1 = findViewById<RecyclerView>(R.id.notif_recyclerview1)
        recyclerView1.layoutManager = LinearLayoutManager(this)
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

        // ✅ RecyclerView for Expired
        val recyclerView2 = findViewById<RecyclerView>(R.id.notif_recyclerview2)
        recyclerView2.layoutManager = LinearLayoutManager(this)
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
    }
}
