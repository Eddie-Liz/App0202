package com.example.app0202

import android.app.Application
import com.example.app0202.di.ServiceLocator

class RootiCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
