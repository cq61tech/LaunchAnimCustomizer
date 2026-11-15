package com.cq61tech.launchanimcustomizer

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.libxposed.service.XposedService
import io.github.libxposed.service.XposedServiceHelper

data class FrameworkInfo(val name: String, val version: String, val api: Int)

object ModuleState : XposedServiceHelper.OnServiceListener {
    var info by mutableStateOf<FrameworkInfo?>(null)
        private set

    override fun onServiceBind(service: XposedService) {
        val details = runCatching {
            FrameworkInfo(
                service.getFrameworkName(),
                service.getFrameworkVersion(),
                service.getAPIVersion()
            )
        }.getOrNull()
        info = details ?: FrameworkInfo("LSPosed", "", 0)
        runCatching { service.getRemotePreferences(Keys.GROUP) }
            .onSuccess { SettingsStore.attachRemote(it) }
            .onFailure { Log.e("LaunchAnim", "remote preferences unavailable", it) }
    }

    override fun onServiceDied(service: XposedService) {
        info = null
        SettingsStore.attachRemote(null)
    }
}
