package com.oslofjorden

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging

@Suppress("Unused")
class OslofjordenApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(applicationContext)
        FirebaseMessaging.getInstance().subscribeToTopic("weather")
    }
}