package com.example.expirease

import android.annotation.SuppressLint
import android.os.Bundle
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

        // Access listOfItems from Application class
//        val app = application as MyApplication
//        val allItems = app.listOfItems

//        val now = System.currentTimeMillis()
//        expiringSoonList.clear()
//        expiredList.clear()
//
//        // Separate items
//        expiringSoonList.addAll(allItems.filter { it.expiryDate >= now })
//        expiredList.addAll(allItems.filter { it.expiryDate < now })
//
//        // RecyclerView for Expiring Soon
//        val recyclerView1 = findViewById<RecyclerView>(R.id.notif_recyclerview1)
//        recyclerView1.layoutManager = LinearLayoutManager(this)
//        expiringSoonAdapter = NotificationRecyclerViewAdapter(expiringSoonList, onClick = { item ->
//            val dialog = NotificationDetailsDialogFragment()
//            dialog.setItemData(item) { itemToRemove ->
//                val index = expiringSoonList.indexOf(itemToRemove)
//                if (index != -1) {
//                    expiringSoonList.removeAt(index)
//                    expiringSoonAdapter.notifyItemRemoved(index)
//                }
//            }
//            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
//        })
//        recyclerView1.adapter = expiringSoonAdapter
//
//        // RecyclerView for Expired
//        val recyclerView2 = findViewById<RecyclerView>(R.id.notif_recyclerview2)
//        recyclerView2.layoutManager = LinearLayoutManager(this)
//        expiredAdapter = NotificationRecyclerViewAdapter(expiredList, onClick = { item ->
//            val dialog = NotificationDetailsDialogFragment()
//            dialog.setItemData(item) { itemToRemove ->
//                val index = expiredList.indexOf(itemToRemove)
//                if (index != -1) {
//                    expiredList.removeAt(index)
//                    expiredAdapter.notifyItemRemoved(index)
//                }
//            }
//            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
//        })
//        recyclerView2.adapter = expiredAdapter


        // Set up RecyclerViews
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

        // Observe LiveData from ViewModel
        viewModel.allItems.observe(this, Observer { itemList ->
            val now = System.currentTimeMillis()

            expiringSoonList.clear()
            expiredList.clear()

            expiringSoonList.addAll(itemList.filter { it.expiryDate >= now })
            expiredList.addAll(itemList.filter { it.expiryDate < now })


            // Now it's safe to notify the adapters
            expiringSoonAdapter.notifyDataSetChanged()
            expiredAdapter.notifyDataSetChanged()
        })
    }
}
