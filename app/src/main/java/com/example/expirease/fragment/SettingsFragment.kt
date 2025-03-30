package com.example.expirease.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.example.expirease.DevelopersActivity
import com.example.expirease.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and store it in a variable
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        // Now, find the button inside the inflated view
        val devButton: ImageView = view.findViewById(R.id.developers_button)

        // Set click listener
        devButton.setOnClickListener {
            val intent = Intent(requireContext(), DevelopersActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}