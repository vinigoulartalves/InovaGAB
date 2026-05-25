package com.fiap.inovagab

import android.app.Application
import com.google.firebase.FirebaseApp

class InovaGabApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}
