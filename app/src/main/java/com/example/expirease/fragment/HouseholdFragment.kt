package com.example.expirease.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Category
import com.example.expirease.data.Item
import com.example.expirease.helperNotif.NotificationDetailsDialogFragment
import com.example.expirease.helperNotif.NotificationRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

class HouseholdFragment : Fragment() {
    private lateinit var listOfItems: MutableList<Item>
    private lateinit var itemAdapter: NotificationRecyclerViewAdapter
    private lateinit var filteredList: MutableList<Item>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_household, container, false)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        listOfItems = mutableListOf(
            Item("Dish Soap", 1, dateFormat.parse("2025-04-20")!!.time, Category.BAKERY, R.drawable.banana),
            Item("Toilet Paper", 3, dateFormat.parse("2025-04-18")!!.time, Category.BAKERY, R.drawable.banana)
        )

        filteredList = listOfItems.toMutableList()

        val recyclerView = view.findViewById<RecyclerView>(R.id.household_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemAdapter = NotificationRecyclerViewAdapter(filteredList, onClick = { item ->
            val dialog = NotificationDetailsDialogFragment()
            val bundle = Bundle().apply {
                putInt("photo", item.photoResource)
                putString("name", item.name)
                putInt("quantity", item.quantity)
            }
            dialog.arguments = bundle
            dialog.show(parentFragmentManager, "NotificationDetailsDialog")
        })

        recyclerView.adapter = itemAdapter

        return view
    }
}
