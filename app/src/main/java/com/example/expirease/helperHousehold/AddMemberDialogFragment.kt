package com.example.expirease.helperHousehold

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.expirease.R
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

class AddMemberDialogFragment : DialogFragment() {

    interface OnMemberAddedListener {
        fun onMemberAdded(username: String, email: String)
    }

    private var addListener: OnMemberAddedListener? = null

    fun setOnMemberAddedListener(listener: OnMemberAddedListener) {
        addListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_add_member, container, false)
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val emailInput = view.findViewById<EditText>(R.id.edit_email)
        val addButton = view.findViewById<Button>(R.id.btn_add_member)

        addButton.setOnClickListener {
            val email = emailInput.text.toString().trim()
            if (email.isNotEmpty()) {
                val usersRef = FirebaseDatabase.getInstance().getReference("Users")
                usersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        var found = false
                        for (userSnapshot in snapshot.children) {
                            val userEmail = userSnapshot.child("email").getValue(String::class.java)
                            val username = userSnapshot.child("username").getValue(String::class.java)
                            if (userEmail == email && username != null) {
                                addListener?.onMemberAdded(username, email)
                                dismiss()
                                found = true
                                break
                            }
                        }
                        if (!found) {
                            emailInput.error = "No user found with that email."
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        emailInput.error = "Database error: ${error.message}"
                    }
                })
            } else {
                emailInput.error = "Email cannot be empty"
            }
        }
    }
}
