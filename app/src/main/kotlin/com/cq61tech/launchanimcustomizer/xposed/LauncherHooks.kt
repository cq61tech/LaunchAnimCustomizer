package com.cq61tech.launchanimcustomizer.xposed

import android.content.SharedPreferences
import android.util.Log
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Presets
import com.cq61tech.launchanimcustomizer.Spring
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Field

class LauncherHooks(
    private val module: XposedModule,
    private val loader: ClassLoader,
    private val prefs: SharedPreferences
) {
    private var logged = 0

    fun install() {
        attempt("close spring") { hookClose() }
        attempt("open spring") { hookOpen() }
    }

    private fun attempt(name: String, block: () -> Unit) {
        try {
            block()
            Log.i(TAG, "hooked $name")
        } catch (t: Throwable) {
            Log.e(TAG, "failed to hook $name", t)
        }
    }

    private fun current(): Config = Config.read(prefs)

    private fun active(config: Config): Boolean =
        config.enabled && config.mode != Presets.CLASSIC

    private fun note(message: String) {
        if (logged < 40 && current().debug) {
            logged++
            Log.d(TAG, message)
        }
    }

    private fun hookClose() {
        val animClass = loader.loadClass("com.android.quickstep.util.RectFSpringAnim")
        val configClass = loader.loadClass("com.android.quickstep.util.RectFSpringAnim\$SpringConfig")
        val constructor = animClass.getDeclaredConstructor(configClass)
        val stiffnessX = field(configClass, "stiffnessX")
        val stiffnessY = field(configClass, "stiffnessY")
        val rectStiffness = field(configClass, "rectStiffness")
        val dampingX = field(configClass, "dampingX")
        val dampingY = field(configClass, "dampingY")
        module.hook(constructor).intercept { chain ->
            try {
                val config = current()
                val target: Any? = chain.getArg(0)
                if (active(config) && target != null) {
                    val spring = config.close
                    stiffnessX.setFloat(target, spring.stiffness)
                    stiffnessY.setFloat(target, spring.stiffness)
                    rectStiffness.setFloat(target, spring.stiffness)
                    dampingX.setFloat(target, spring.damping)
                    dampingY.setFloat(target, spring.damping)
                    note("close spring ${spring.stiffness} / ${spring.damping}")
                }
            } catch (t: Throwable) {
                Log.e(TAG, "close spring override failed", t)
            }
            chain.proceed()
        }
    }

    private fun hookOpen() {
        val multi = loader.loadClass(
            "com.android.systemui.animation.TransitionAnimator\$MultiSpringAnimation"
        )
        val start = multi.getDeclaredMethod("initAndStartSprings", java.lang.Long.TYPE)
        module.hook(start).intercept { chain ->
            val owner = chain.thisObject
            retune(multi, owner)
            val result = chain.proceed()
            retune(multi, owner)
            result
        }
    }

    private fun retune(multi: Class<*>, owner: Any?) {
        if (owner == null) return
        try {
            val config = current()
            if (!active(config)) return
            val spring = config.open
            for (getter in arrayOf("getSpringX", "getSpringY", "getSpringScale")) {
                val animation = multi.getMethod(getter).invoke(owner) ?: continue
                apply(animation, spring)
            }
            note("open spring ${spring.stiffness} / ${spring.damping}")
        } catch (t: Throwable) {
            Log.e(TAG, "open spring override failed", t)
        }
    }

    private fun apply(animation: Any, spring: Spring) {
        val force = animation.javaClass.getMethod("getSpring").invoke(animation) ?: return
        val floatType = java.lang.Float.TYPE
        force.javaClass.getMethod("setStiffness", floatType).invoke(force, spring.stiffness)
        force.javaClass.getMethod("setDampingRatio", floatType).invoke(force, spring.damping)
    }

    private fun field(owner: Class<*>, name: String): Field =
        owner.getDeclaredField(name).apply { isAccessible = true }

    companion object {
        private const val TAG = ModuleMain.TAG
    }
}
