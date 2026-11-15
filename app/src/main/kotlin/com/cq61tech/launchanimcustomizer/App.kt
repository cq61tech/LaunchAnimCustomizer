package com.cq61tech.launchanimcustomizer

import android.app.Application
import io.github.libxposed.service.XposedServiceHelper

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        SettingsStore.load(this)
        XposedServiceHelper.registerListener(ModuleState)
    }
}
