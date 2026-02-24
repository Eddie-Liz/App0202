package com.rootilabs.wmeCardiac

import android.app.Application
import com.rootilabs.wmeCardiac.di.ServiceLocator

class RootiCareApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.init(this)
    }
}
