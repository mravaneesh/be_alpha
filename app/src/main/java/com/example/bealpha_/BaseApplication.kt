package com.example.bealpha_

import android.app.Application
import com.amazonaws.mobileconnectors.s3.transferutility.TransferNetworkLossHandler
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class BaseApplication: Application() {
    override fun onCreate() {
        super.onCreate()
        TransferNetworkLossHandler.getInstance(applicationContext)
    }
}