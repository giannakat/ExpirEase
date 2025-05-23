package com.example.expirease.pager

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.ItemStatus
import com.example.expirease.helper.HistoryAdapter
import com.example.expirease.viewmodel.SharedItemViewModel

class ExpiredItemFragment : Fragment() {
    private val sharedItemViewModel: SharedItemViewModel by activityViewModels()

    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_history_expired_item, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        recyclerView = view.findViewById(R.id.recycler_view_history)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        historyAdapter = HistoryAdapter(
            items = emptyList(),
            onRestore = { item ->
                sharedItemViewModel.restoreItem(item)
                Toast.makeText(requireContext(), "${item.name} restored!", Toast.LENGTH_SHORT).show()
            },
            onDelete = { item ->
                sharedItemViewModel.deleteItem(item)
                Toast.makeText(requireContext(), "${item.name} deleted!", Toast.LENGTH_SHORT).show()
            }
        )

        recyclerView.adapter = historyAdapter

        sharedItemViewModel.allItems.observe(viewLifecycleOwner) { items ->
            historyAdapter.submitList(items.filter { it.status == ItemStatus.EXPIRED })
        }
    }
}
