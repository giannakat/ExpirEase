package com.example.expirease.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.expirease.R
import com.example.expirease.data.HistoryItem
import com.example.expirease.helper.HistoryAdapter
import com.example.expirease.helperNotif.HistoryPagerAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator

class HistoryFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter : HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    private lateinit var viewPager: ViewPager2
    private lateinit var tabLayout: TabLayout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    //    recyclerView = view.findViewById(R.id.recycler_view_history)
    //    recyclerView.layoutManager = LinearLayoutManager(requireContext())

//        historyAdapter = HistoryAdapter(historyList){item ->
//            //Toast.makeText(this, "${item.name} restored!", Toast.LENGTH_SHORT).show()
//        }
//        recyclerView.adapter = historyAdapter
//
//        loadMockData() // for now
        viewPager = view.findViewById<ViewPager2>(R.id.viewPager)
        tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        val adapter = HistoryPagerAdapter(this)
        viewPager.adapter = adapter

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "All"
                1 -> "Expired"
                2 -> "Consumed"
                else -> ""
            }
        }.attach()
    }

//    private fun loadMockData() {
//        historyList.add(HistoryItem("Banana", "Expired", "2025-04-01"))
//        historyList.add(HistoryItem("Milk", "Deleted", "2025-04-02"))
//        historyList.add(HistoryItem("Rice", "Consumed", "2025-04-03"))
//        historyAdapter.notifyDataSetChanged()
//    }
}