package com.cq61tech.launchanimcustomizer.xposed

import android.util.Log
import com.cq61tech.launchanimcustomizer.Keys
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface

class ModuleMain : XposedModule() {
    override fun onPackageReady(param: XposedModuleInterface.PackageReadyParam) {
        if (param.packageName !in TARGETS) return
        runCatching {
            val prefs = getRemotePreferences(Keys.GROUP)
            LauncherHooks(this, param.classLoader, prefs).install()
        }.onFailure {
            Log.e(TAG, "setup failed in ${param.packageName}", it)
        }
    }

    companion object {
        const val TAG = "LaunchAnim"
        private val TARGETS = setOf(
            "com.google.android.apps.nexuslauncher",
            "com.android.launcher3"
        )
    }
}
