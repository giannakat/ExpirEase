package com.example.expirease.data

import com.example.expirease.R

data class Member(
    var id: String = "", // Firebase usually generates a String key for each item
    var username: String = "",
    var email: String = "",
    var photoResource: Int = R.drawable.ic_person // Default photo
)
