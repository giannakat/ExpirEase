package com.example.expirease.app

import android.app.Application
import android.util.Log
import com.example.expirease.data.Item

class MyApplication : Application() {

    var username: String = ""
    var password: String = ""
    var email: String = ""
    var name: String = "Gianna Carreon"

    // List of items shared across the app
    var listOfItems: MutableList<Item> = mutableListOf()

    override fun onCreate() {
        super.onCreate()
        Log.e("Test", "My application is called")
    }
}
