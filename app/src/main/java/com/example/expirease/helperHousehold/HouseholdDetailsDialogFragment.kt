package com.example.expirease.helperHousehold

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.example.expirease.R

class HouseholdDetailsDialogFragment : DialogFragment() {

    interface OnMemberRemovedListener {
        fun onRemoveMember(firstname: String, lastname: String)
    }

    private var removeListener: OnMemberRemovedListener? = null

    fun setOnRemoveListener(listener: OnMemberRemovedListener) {
        this.removeListener = listener
    }

    override fun onResume() {
        super.onResume()
        dialog?.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_household_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val memberPhoto = view.findViewById<ImageView>(R.id.member_photo)
        val memberFirstName = view.findViewById<TextView>(R.id.member_firstname)
        val memberLastName = view.findViewById<TextView>(R.id.member_lastname)
        val removeButton = view.findViewById<Button>(R.id.add) // Button is actually for "Remove"

        arguments?.let {
            val photo = it.getInt("photo", R.drawable.img_product_banana)
            val firstname = it.getString("firstname") ?: "Unknown"
            val lastname = it.getString("lastname") ?: "Unknown"

            memberPhoto.setImageResource(photo)
            memberFirstName.text = firstname
            memberLastName.text = lastname

            removeButton.setOnClickListener {
                removeListener?.onRemoveMember(firstname, lastname)
                dismiss()
            }
        }
    }
}
