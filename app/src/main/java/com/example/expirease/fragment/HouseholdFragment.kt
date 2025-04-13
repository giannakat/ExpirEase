package com.example.expirease.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Member
import com.example.expirease.helperHousehold.AddMemberDialogFragment
import com.example.expirease.helperHousehold.HouseholdDetailsDialogFragment
import com.example.expirease.helperHousehold.HouseholdRecyclerViewAdapter
import java.text.SimpleDateFormat
import java.util.*

class HouseholdFragment : Fragment() {
    private lateinit var listOfItems: MutableList<Member>
    private lateinit var itemAdapter: HouseholdRecyclerViewAdapter
    private lateinit var filteredList: MutableList<Member>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_household, container, false)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        listOfItems = mutableListOf(
            Member("JM", "Casipong", R.drawable.banana),
            Member("JM", "Casipong", R.drawable.banana)
        )

        filteredList = listOfItems.toMutableList()

        val recyclerView = view.findViewById<RecyclerView>(R.id.household_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        itemAdapter = HouseholdRecyclerViewAdapter(filteredList, onClick = { member ->
            val dialog = HouseholdDetailsDialogFragment()
            val bundle = Bundle().apply {
                putInt("photo", member.photoResource)
                putString("firstname", member.firstname)
                putString("lastname", member.lastname)
            }
            dialog.arguments = bundle

            dialog.setOnRemoveListener(object : HouseholdDetailsDialogFragment.OnMemberRemovedListener {
                override fun onRemoveMember(firstname: String, lastname: String) {
                    val memberToRemove = listOfItems.find {
                        it.firstname == firstname && it.lastname == lastname
                    }
                    if (memberToRemove != null) {
                        listOfItems.remove(memberToRemove)
                        filteredList.remove(memberToRemove)
                        itemAdapter.notifyDataSetChanged()
                    }
                }
            })

            dialog.show(parentFragmentManager, "HouseholdDetailsDialog")
        })

        recyclerView.adapter = itemAdapter

        // Handle "+" button
        val addButton = view.findViewById<View>(R.id.add)
        addButton.setOnClickListener {
            val addDialog = AddMemberDialogFragment()
            addDialog.setOnMemberAddedListener(object : AddMemberDialogFragment.OnMemberAddedListener {
                override fun onMemberAdded(firstname: String, lastname: String) {
                    val newMember = Member(firstname, lastname, R.drawable.banana) // default image
                    listOfItems.add(newMember)
                    filteredList.add(newMember)
                    itemAdapter.notifyDataSetChanged()
                }
            })
            addDialog.show(parentFragmentManager, "AddMemberDialog")
        }

        return view
    }
}
