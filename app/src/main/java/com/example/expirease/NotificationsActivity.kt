package com.example.expirease

import java.text.SimpleDateFormat
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helperNotif.NotificationDetailsDialogFragment
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter
import java.util.Locale

class NotificationsActivity : AppCompatActivity() {
    private lateinit var listOfItems: MutableList<Item>
    private lateinit var itemAdapter: NotificationRecyclerViewAdapter
    private lateinit var filteredList: MutableList<Item>

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notifications)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        // ✅ Initialize the list before using it
        listOfItems = mutableListOf(
            Item("Egg", 2, dateFormat.parse("2025-04-05")!!.time, Category.BAKERY, R.drawable.banana),
            Item("Egg", 2, dateFormat.parse("2025-04-05")!!.time, Category.BAKERY, R.drawable.banana)
        )

        filteredList = listOfItems.toMutableList()

        val recyclerView = findViewById<RecyclerView>(R.id.notif_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(this)

        itemAdapter = NotificationRecyclerViewAdapter(filteredList, onClick = { item ->
            val dialog = NotificationDetailsDialogFragment()
            val bundle = Bundle().apply {
                putInt("photo", item.photoResource)
                putString("name", item.name)
                putLong("expiry", item.expiryDate)
            }
            dialog.arguments = bundle
            dialog.show(supportFragmentManager, "NotificationDetailsDialog")
        })

        recyclerView.adapter = itemAdapter
    }
}
