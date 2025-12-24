package com.example.tvtvapp

import android.app.Application
import com.logituit.logixsdk.logixplayer.LogixPlayerSDK

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Initialize SDK — if SDK requires API key or config, pass it here.
        // Replace or extend the call if the SDK initialize method requires parameters.
        try {
            LogixPlayerSDK.initialize(this)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
