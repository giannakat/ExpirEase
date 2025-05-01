package com.example.expirease.fragment

import android.app.AlertDialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.example.expirease.DevelopersActivity
import com.example.expirease.ProfileActivity
import com.example.expirease.R

class SettingsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout and store it in a variable
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        //preference
        val darkmodeBtn: LinearLayout = view.findViewById(R.id.preference_darkmode)
        val languageBtn: LinearLayout = view.findViewById(R.id.preference_language)
        val reminderBtn: LinearLayout = view.findViewById(R.id.preference_reminder)

        //darkmode
        darkmodeBtn.setOnClickListener {
            //TODO add toggle options here
        }

        //reminder options
        reminderBtn.setOnClickListener {
            val reminderOptions = arrayOf("1 day before", "2 days before", "3 days before", "1 week before")
            var selectedOption = 0 // default selected index

            AlertDialog.Builder(requireContext())
                .setTitle("Reminder Timing")
                .setSingleChoiceItems(reminderOptions, selectedOption) { _, which ->
                    selectedOption = which // store the selected option
                }
                .setPositiveButton("OK") { dialog, _ ->
                    val selectedReminder = reminderOptions[selectedOption]
                    // Do something with the selected reminder range (e.g., save to ViewModel or SharedPreferences)
                    Toast.makeText(requireContext(), "Selected: $selectedReminder", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        // Show language option dialog (only English)
        languageBtn.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Select Language")
                .setSingleChoiceItems(arrayOf("English"), 0, null)
                .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
                .show()
        }

        val devButton: LinearLayout = view.findViewById(R.id.feedback_developers)
        val profileBtn: LinearLayout = view.findViewById(R.id.general_editprofile)

        val passwordBtn: LinearLayout = view.findViewById(R.id.general_changepassword)



        // Navigate to DevelopersActivity
        devButton.setOnClickListener {
            val intent = Intent(requireContext(), DevelopersActivity::class.java)
            startActivity(intent)
        }

        // Navigate to EditProfileActivity
        profileBtn.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }

        // Navigate to EditProfileActivity
        passwordBtn.setOnClickListener {
            val intent = Intent(requireContext(), ProfileActivity::class.java)
            startActivity(intent)
        }


        //feedback options
        val reportBugBtn: LinearLayout = view.findViewById(R.id.feedback_reportBug)
        val rateUsBtn: LinearLayout = view.findViewById(R.id.feedback_rateUs)
        val sendFeedbackBtn: LinearLayout = view.findViewById(R.id.feedback_sendFeedback)

        sendFeedbackBtn.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("gkcarreon@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Feedback for ExpirEase")
            }

            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            } else {
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
            }
        }


        reportBugBtn.setOnClickListener {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf("gkcarreon@gmail.com"))
                putExtra(Intent.EXTRA_SUBJECT, "Bug Report")
                putExtra(Intent.EXTRA_TEXT, "Describe the bug here:\n\nSteps to reproduce:\n1.\n2.\n3.\n\nExpected result:\n\nActual result:")
            }

            if (emailIntent.resolveActivity(requireActivity().packageManager) != null) {
                startActivity(emailIntent)
            } else {
                Toast.makeText(requireContext(), "No email app found", Toast.LENGTH_SHORT).show()
            }
        }


        rateUsBtn.setOnClickListener {
            val packageName = requireContext().packageName
            try {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")))
            } catch (e: ActivityNotFoundException) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")))
            }
        }



        return view
    }
}