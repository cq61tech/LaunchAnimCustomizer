package com.cq61tech.launchanimcustomizer.xposed

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Keys
import com.cq61tech.launchanimcustomizer.OpenSettings
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Field
import kotlin.math.max

class LauncherHooks(
    private val module: XposedModule,
    private val loader: ClassLoader,
    private val prefs: SharedPreferences
) {
    private class Snapshot(val raw: String?, val config: Config, val curve: CurveInterpolator)

    @Volatile
    private var snapshot = Snapshot(null, Config(), CurveInterpolator(OpenSettings()))

    private val closeCounter = ThreadLocal<IntArray?>()
    private var originalPrimary: Any? = null
    private var originalHorizontal: Any? = null
    private var logged = 0

    @Volatile
    private var context: Context? = null

    fun install() {
        val seen = current().config
        Log.i(TAG, "config enabled=${seen.enabled} mode=${seen.mode} keys=${prefs.all.size}")
        attempt("open animation") { hookOpen() }
        attempt("close animation") { hookClose() }
    }

    private fun attempt(name: String, block: () -> Unit) {
        try {
            block()
            Log.i(TAG, "hooked $name")
        } catch (t: Throwable) {
            Log.e(TAG, "failed to hook $name", t)
        }
    }

    private fun current(): Snapshot {
        val text = prefs.getString(Keys.CONFIG, null)
        val old = snapshot
        if (text == old.raw) return old
        val config = Config.fromRaw(text)
        val fresh = Snapshot(text, config, CurveInterpolator(config.open))
        snapshot = fresh
        return fresh
    }

    private fun note(message: String) {
        if (logged < 24 || (logged < 80 && current().config.debug)) {
            logged++
            Log.i(TAG, message)
        }
    }

    private fun hookOpen() {
        val runner = loader.loadClass(
            "com.android.launcher3.QuickstepTransitionManager\$AppLaunchAnimationRunner"
        )
        val manager = loader.loadClass("com.android.launcher3.QuickstepTransitionManager")
        val outer = field(runner, "this\$0")
        val primary = field(manager, "mOpeningInterpolator")
        val horizontal = field(manager, "mOpeningXInterpolator")
        val start = runner.declaredMethods.first {
            it.name == "onAnimationStart" && !it.isSynthetic && !it.isBridge
        }
        module.hook(start).intercept { chain ->
            try {
                val owner = outer.get(chain.thisObject)
                if (owner != null) {
                    if (originalPrimary == null) {
                        originalPrimary = primary.get(owner)
                        originalHorizontal = horizontal.get(owner)
                    }
                    val snap = current()
                    if (snap.config.active) {
                        primary.set(owner, snap.curve)
                        horizontal.set(owner, snap.curve)
                        note("open curve ${snap.config.open.curve}")
                    } else {
                        primary.set(owner, originalPrimary)
                        horizontal.set(owner, originalHorizontal)
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "open override failed", t)
            }
            chain.proceed()
        }
    }

    private fun hookClose() {
        val anim = loader.loadClass("com.android.quickstep.util.RectFSpringAnim")
        val resource = loader.loadClass("com.android.launcher3.util.DynamicResource")
        val getFloat = resource.getDeclaredMethod("getFloat", Integer.TYPE)
        val starts = anim.declaredMethods.filter { it.name == "start" }
        check(starts.isNotEmpty()) { "start not found" }
        starts.forEach { method ->
            module.hook(method).intercept { chain ->
                val first: Any? = chain.args.firstOrNull()
                if (first is Context) context = first.applicationContext
                closeCounter.set(IntArray(1))
                try {
                    chain.proceed()
                } finally {
                    closeCounter.remove()
                }
            }
        }
        module.hook(getFloat).intercept { chain ->
            val original = chain.proceed()
            val state = closeCounter.get()
            if (state == null || original !is Float) {
                original
            } else {
                val id: Any? = chain.args.firstOrNull()
                tune(state, original, if (id is Int) id else 0)
            }
        }
    }

    private fun tune(state: IntArray, value: Float, id: Int): Float {
        state[0]++
        val index = state[0]
        val config = current().config
        if (!config.active) return value
        val close = config.close
        val axis = when (index) {
            2, 3 -> close.x
            4, 5 -> if (close.split) close.y else close.x
            6, 7 -> if (close.split) close.scale else close.x
            else -> return value
        }
        val speed = close.speed.coerceIn(0.2f, 4f)
        val stiffness = index % 2 == 0
        val result = when {
            stiffness && value >= 5f -> max(1f, axis.stiffness * speed * speed)
            !stiffness && value <= 5f -> axis.damping
            else -> value
        }
        note("close #$index ${resourceName(id)} $value -> $result")
        return result
    }

    private fun resourceName(id: Int): String {
        val owner = context ?: return id.toString()
        return runCatching { owner.resources.getResourceEntryName(id) }.getOrDefault(id.toString())
    }

    private fun field(owner: Class<*>, name: String): Field =
        owner.getDeclaredField(name).apply { isAccessible = true }

    companion object {
        private const val TAG = ModuleMain.TAG
    }
}
