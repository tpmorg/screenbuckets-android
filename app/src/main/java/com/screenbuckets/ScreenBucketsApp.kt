package com.screenbuckets

import android.app.Application
import timber.log.Timber

class ScreenBucketsApp : Application() {
    
    companion object {
        // For singleton access to application context
        lateinit var instance: ScreenBucketsApp
            private set
    }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Initialize Timber for logging
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }
}