package com.example.expirease.helperHousehold

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.DialogFragment
import com.example.expirease.R

class AddMemberDialogFragment : DialogFragment() {

    interface OnMemberAddedListener {
        fun onMemberAdded(firstname: String, lastname: String)
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
        val firstNameInput = view.findViewById<EditText>(R.id.edit_firstname)
        val lastNameInput = view.findViewById<EditText>(R.id.edit_lastname)
        val addButton = view.findViewById<Button>(R.id.btn_add_member)

        addButton.setOnClickListener {
            val firstname = firstNameInput.text.toString().trim()
            val lastname = lastNameInput.text.toString().trim()
            if (firstname.isNotEmpty() && lastname.isNotEmpty()) {
                addListener?.onMemberAdded(firstname, lastname)
                dismiss()
            }
        }
    }
}
