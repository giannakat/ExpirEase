package com.example.expirease.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.expirease.R
import com.example.expirease.data.Member
import com.example.expirease.helperHousehold.AddMemberDialogFragment
import com.example.expirease.helperHousehold.HouseholdDetailsDialogFragment
import com.example.expirease.helperHousehold.HouseholdRecyclerViewAdapter
import com.google.firebase.database.*

class HouseholdFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var itemAdapter: HouseholdRecyclerViewAdapter
    private lateinit var listOfItems: MutableList<Member>
    private lateinit var database: DatabaseReference

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_household, container, false)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().getReference("householdMembers")

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.household_recyclerview)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Set up RecyclerView Adapter
        listOfItems = mutableListOf()
        itemAdapter = HouseholdRecyclerViewAdapter(listOfItems, onClick = { member ->
            val dialog = HouseholdDetailsDialogFragment()
            val bundle = Bundle().apply {
                putString("id", member.id)
                putString("firstname", member.username)
                putString("lastname", member.email)
                putInt("photo", member.photoResource)
            }
            dialog.arguments = bundle

            dialog.setOnRemoveListener(object : HouseholdDetailsDialogFragment.OnMemberRemovedListener {
                override fun onRemoveMember(id: String) {
                    removeMemberFromDatabase(id)
                }
            })

            dialog.show(parentFragmentManager, "HouseholdDetailsDialog")
        })
        recyclerView.adapter = itemAdapter

        // Add Member Button
        val addButton = view.findViewById<View>(R.id.add)
        addButton.setOnClickListener {
            val addDialog = AddMemberDialogFragment()
            addDialog.setOnMemberAddedListener(object : AddMemberDialogFragment.OnMemberAddedListener {
                override fun onMemberAdded(username: String, email: String) {
                    val memberId = database.push().key
                    if (memberId != null) {
                        val newMember = Member(
                            id = memberId,
                            username = username,
                            email = email,
                            photoResource = R.drawable.img_placeholder_user // Default image
                        )
                        database.child(memberId).setValue(newMember).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                showToast("Member added")
                            } else {
                                showToast("Error adding member")
                            }
                        }
                    } else {
                        showToast("Error generating member ID")
                    }
                }
            })
            addDialog.show(parentFragmentManager, "AddMemberDialog")
        }


        // Fetch members from Firebase
        fetchMembersFromDatabase()

        return view
    }

    // Fetch members from Firebase and update the list
    private fun fetchMembersFromDatabase() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                listOfItems.clear() // Clear current list
                for (memberSnapshot in snapshot.children) {
                    val member = memberSnapshot.getValue(Member::class.java)
                    member?.let {
                        listOfItems.add(it)
                    }
                }
                itemAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                showToast("Error fetching members: ${error.message}")
            }
        })
    }

    // Add a new member to Firebase
    private fun addMemberToDatabase(member: Member) {
        val memberId = database.push().key
        if (memberId != null) {
            database.child(memberId).setValue(member).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    showToast("Member added")
                } else {
                    showToast("Error adding member")
                }
            }
        }
    }

    // Remove a member from Firebase
    private fun removeMemberFromDatabase(id: String) {
        database.child(id).removeValue().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                showToast("Member removed")
            } else {
                showToast("Error removing member")
            }
        }
    }

    private fun showToast(message: String) {
        if (isAdded) {
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        }
    }
}
