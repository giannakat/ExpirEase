package com.example.expirease

import android.app.Activity
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.data.Item
import com.example.expirease.helper.ItemDetailsDialogFragment
import com.example.expirease.helper.ItemRecyclerViewAdapter
import com.example.expirease.helperNotif.NotificationDetailsDialogFragment
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter

class NotificationsActivity : AppCompatActivity() {  // Changed to AppCompatActivity for better compatibility
    private lateinit var listOfItems: MutableList<Item>
    private lateinit var itemAdapter: NotificationRecyclerViewAdapter // Use correct adapter
    private lateinit var filteredList: MutableList<Item>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

//        listOfItems = mutableListOf(
//            Item("Egg", 2, R.drawable.banana),
//            Item("Milk", 1, R.drawable.banana),
//            Item("Bread", 3, R.drawable.banana),
//            Item("Rice", 5, R.drawable.banana),
//            Item("Apple", 4, R.drawable.banana),
//            Item("Chicken", 2, R.drawable.banana)
//        )

        filteredList = listOfItems.toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.item_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        itemAdapter = NotificationRecyclerViewAdapter(filteredList, onClick = { item ->
            val dialog = NotificationDetailsDialogFragment()
            val bundle = Bundle().apply {
                putInt("photo", item.photoResource)
                putString("name", item.name)
                putInt("quantity", item.quantity)
            }
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
        })

        recyclerView.adapter = itemAdapter // 🔥 **This was missing**
    }
}
