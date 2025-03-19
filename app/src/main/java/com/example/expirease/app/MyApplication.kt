package com.example.expirease.app

import android.app.Application
import android.util.Log

class MyApplication : Application(){

    var username : String = ""
    var password : String = ""
    var email : String = ""
    var name : String = "Gianna Carreon"


    override fun onCreate() {
        super.onCreate()
        Log.e("Test", "My application is called")
    }
}