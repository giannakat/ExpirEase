package com.example.expirease.pager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.HistoryItem
import com.example.expirease.helper.HistoryAdapter

class AllItemFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val historyList = mutableListOf<HistoryItem>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_all_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter(historyList) { item ->
            Toast.makeText(requireContext(), "${item.name} restored!", Toast.LENGTH_SHORT).show()
            // You can also call a restore function here if needed
        }

        recyclerView.adapter = historyAdapter
        loadMockData()
    }

    private fun loadMockData() {
        historyList.add(HistoryItem("Banana", "Expired", "2025-04-01"))
        historyList.add(HistoryItem("Milk", "Deleted", "2025-04-02"))
        historyList.add(HistoryItem("Rice", "Consumed", "2025-04-03"))
        historyAdapter.notifyDataSetChanged()
    }
}