package com.example.expirease

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ListViewActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_household)

        //create a listview in xml
        //in activity instantiate first the list view
        //initialize data source
        //user arrayadapter from android library
        //send the array adapter to our listview


        val listView = findViewById<ListView>(R.id.household_listview)
        val memberList = listOf("Maria", "Jose", "hahah", "berto", "Gie")
        val arrayAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, memberList)
        listView.adapter = arrayAdapter

        Log.d("ListViewActivity", "Adapter item count: ${arrayAdapter.count}")


        listView.setOnItemClickListener { _, _, position, _ ->
            Toast.makeText(this, "Item $position with data ${memberList[position]}" , Toast.LENGTH_LONG).show()
        }
    }
}