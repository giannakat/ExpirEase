package com.example.expirease

import android.app.Activity
import android.os.Bundle
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.expirease.data.Item
import com.example.expirease.helper.ItemsCustomListViewAdapter

class CustomListViewActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_category)

        //widget
        val listView = findViewById<ListView>(R.id.category_listview)

        val listOfItems = listOf(
            Item("Egg", 2, R.drawable.banana),
            Item("Milk", 1, R.drawable.banana),
            Item("Bread", 3, R.drawable.banana),
            Item("Rice", 5, R.drawable.banana),
            Item("Apple", 4, R.drawable.banana),
            Item("Chicken", 2, R.drawable.banana),
            Item("Fish", 3, R.drawable.banana),
            Item("Carrot", 6, R.drawable.banana),
            Item("Potato", 7, R.drawable.banana),
            Item("Tomato", 3, R.drawable.banana),
            Item("Onion", 4, R.drawable.banana),
            Item("Garlic", 2, R.drawable.banana),
            Item("Cheese", 1, R.drawable.banana),
            Item("Butter", 2, R.drawable.banana),
            Item("Yogurt", 3, R.drawable.banana)
        )

        val adapter: ItemsCustomListViewAdapter = ItemsCustomListViewAdapter(this, listOfItems)
        listView.adapter = adapter
    }
}