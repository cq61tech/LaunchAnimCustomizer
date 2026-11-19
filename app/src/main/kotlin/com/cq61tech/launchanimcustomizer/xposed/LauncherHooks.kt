package com.cq61tech.launchanimcustomizer.xposed

import android.animation.ValueAnimator
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import android.view.animation.Interpolator
import com.cq61tech.launchanimcustomizer.Config
import com.cq61tech.launchanimcustomizer.Keys
import io.github.libxposed.api.XposedModule
import java.lang.reflect.Field
import kotlin.math.max

class LauncherHooks(
    private val module: XposedModule,
    private val loader: ClassLoader,
    private val prefs: SharedPreferences
) {
    private class Snapshot(
        val raw: String?,
        val config: Config,
        val main: Interpolator,
        val horizontal: Interpolator,
        val corner: Interpolator,
        val fade: Interpolator
    )

    @Volatile
    private var snapshot = build(null)

    private val closeCounter = ThreadLocal<IntArray?>()
    private val opening = ThreadLocal<Boolean?>()
    private val resizing = ThreadLocal<Boolean?>()
    private var originalPrimary: Any? = null
    private var originalHorizontal: Any? = null
    private var logged = 0

    @Volatile
    private var context: Context? = null

    private lateinit var propClass: Class<*>
    private lateinit var propStart: Field
    private lateinit var propEnd: Field
    private lateinit var propInterpolator: Field

    fun install() {
        val seen = current().config
        Log.i(TAG, "config enabled=${seen.enabled} mode=${seen.mode} keys=${prefs.all.size}")
        attempt("open animation") { hookOpen() }
        attempt("open duration") { hookDuration() }
        attempt("open properties") { hookProperties() }
        attempt("wallpaper blur") { hookBlur() }
        attempt("close springs") { hookClose() }
        attempt("close corners") { hookCloseCorners() }
    }

    private fun attempt(name: String, block: () -> Unit) {
        try {
            block()
            Log.i(TAG, "hooked $name")
        } catch (t: Throwable) {
            Log.e(TAG, "failed to hook $name", t)
        }
    }

    private fun build(raw: String?): Snapshot {
        val config = Config.fromRaw(raw)
        val open = config.open
        val main = CurveInterpolator(open.main, open.duration)
        val horizontal = open.horizontal?.let { CurveInterpolator(it, open.duration) } ?: main
        val corner = open.corner?.let { CurveInterpolator(it, open.duration) } ?: main
        return Snapshot(raw, config, main, horizontal, corner, FadeInterpolator(open.fadeStart, open.fadeEnd))
    }

    private fun current(): Snapshot {
        val text = prefs.getString(Keys.CONFIG, null)
        val old = snapshot
        if (text == old.raw) return old
        val fresh = build(text)
        snapshot = fresh
        return fresh
    }

    private fun note(message: String) {
        if (logged < 30 || (logged < 120 && current().config.debug)) {
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
            opening.set(true)
            try {
                val owner = outer.get(chain.thisObject)
                if (owner != null) {
                    if (originalPrimary == null) {
                        originalPrimary = primary.get(owner)
                        originalHorizontal = horizontal.get(owner)
                    }
                    val snap = current()
                    if (snap.config.customized) {
                        primary.set(owner, snap.main)
                        horizontal.set(owner, snap.horizontal)
                        note("open ${snap.config.open.main.curve} ${snap.config.open.duration}ms")
                    } else {
                        primary.set(owner, originalPrimary)
                        horizontal.set(owner, originalHorizontal)
                    }
                }
            } catch (t: Throwable) {
                Log.e(TAG, "open override failed", t)
            }
            try {
                chain.proceed()
            } finally {
                opening.remove()
            }
        }
    }

    private fun hookDuration() {
        val method = ValueAnimator::class.java.getDeclaredMethod("setDuration", java.lang.Long.TYPE)
        module.hook(method).intercept { chain ->
            val result = chain.proceed()
            if (opening.get() == true && resizing.get() != true) {
                val requested: Any? = chain.args.firstOrNull()
                val snap = current()
                val config = snap.config
                if (requested == LAUNCH_DURATION && (config.customized || config.speedActive)) {
                    val target = chain.thisObject
                    if (target is ValueAnimator) {
                        val base = if (config.customized) config.open.duration else LAUNCH_DURATION.toFloat()
                        val scaled = (base / config.speedFactor).toLong().coerceAtLeast(30L)
                        resizing.set(true)
                        try {
                            target.duration = scaled
                        } finally {
                            resizing.remove()
                        }
                    }
                }
            }
            result
        }
    }

    private fun hookProperties() {
        propClass = loader.loadClass("com.android.quickstep.util.MultiValueUpdateListener\$FloatProp")
        propStart = field(propClass, "mStart")
        propEnd = field(propClass, "mEnd")
        propInterpolator = field(propClass, "mInterpolator")
        val method = ValueAnimator::class.java.getDeclaredMethod(
            "addUpdateListener",
            ValueAnimator.AnimatorUpdateListener::class.java
        )
        module.hook(method).intercept { chain ->
            if (opening.get() == true) {
                val listener: Any? = chain.args.firstOrNull()
                if (listener != null) {
                    try {
                        tuneProperties(listener)
                    } catch (t: Throwable) {
                        Log.e(TAG, "property override failed", t)
                    }
                }
            }
            chain.proceed()
        }
    }

    private fun tuneProperties(listener: Any) {
        val type = listener.javaClass
        if (type.superclass?.name != MULTI_VALUE) return
        val snap = current()
        val config = snap.config
        if (!config.customized) return
        val open = config.open
        for (member in type.declaredFields) {
            if (member.type != propClass) continue
            member.isAccessible = true
            val prop = member.get(listener) ?: continue
            when (member.name) {
                "mIconAlpha" -> propInterpolator.set(prop, snap.fade)
                "mWindowRadius" -> {
                    if (open.corner != null) propInterpolator.set(prop, snap.corner)
                    scale(prop, open.roundness)
                }
                "mCornerRadiusProgress" ->
                    if (open.corner != null) propInterpolator.set(prop, snap.corner)
                "mShadowRadius" -> scale(prop, open.shadow)
                "mDx" -> if (open.originX != 1f) pull(prop, open.originX, false)
                "mDy" -> if (open.originY != 1f) pull(prop, open.originY, false)
                "mCropRectCenterX" -> if (open.originX != 1f) pull(prop, open.originX, true)
                "mCropRectCenterY" -> if (open.originY != 1f) pull(prop, open.originY, true)
            }
        }
        note("open props tuned round=${open.roundness} shadow=${open.shadow}")
    }

    private fun pull(prop: Any, factor: Float, around: Boolean) {
        val start = propStart.getFloat(prop)
        val end = if (around) propEnd.getFloat(prop) else 0f
        propStart.setFloat(prop, end + (start - end) * factor)
    }

    private fun hookBlur() {
        val depth = loader.loadClass("com.android.quickstep.util.BaseDepthController")
        val method = depth.getDeclaredMethod("mapDepthToBlur", java.lang.Float.TYPE)
        module.hook(method).intercept { chain ->
            val result = chain.proceed()
            val config = current().config
            if (config.blurActive && result is Float) result * config.blur else result
        }
    }

    private fun scale(prop: Any, factor: Float) {
        propStart.setFloat(prop, propStart.getFloat(prop) * factor)
        propEnd.setFloat(prop, propEnd.getFloat(prop) * factor)
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

    private fun hookCloseCorners() {
        var hooked = 0
        for (name in CLOSE_RUNNERS) {
            val type = runCatching { loader.loadClass(name) }.getOrNull() ?: continue
            val start = runCatching { field(type, "mStartRadius") }.getOrNull() ?: continue
            val end = runCatching { field(type, "mEndRadius") }.getOrNull() ?: continue
            for (constructor in type.declaredConstructors) {
                module.hook(constructor).intercept { chain ->
                    val result = chain.proceed()
                    try {
                        val config = current().config
                        val target = chain.thisObject
                        if (config.customized && target != null) {
                            val factor = config.close.roundness
                            start.setFloat(target, start.getFloat(target) * factor)
                            end.setFloat(target, end.getFloat(target) * factor)
                            note("close corners x$factor")
                        }
                    } catch (t: Throwable) {
                        Log.e(TAG, "close corner override failed", t)
                    }
                    result
                }
                hooked++
            }
        }
        check(hooked > 0) { "no close runner found" }
    }

    private fun tune(state: IntArray, value: Float, id: Int): Float {
        state[0]++
        val index = state[0]
        if (index < 2 || index > 7) return value
        val config = current().config
        if (!config.customized && !config.speedActive) return value
        val factor = config.speedFactor
        val squared = factor * factor
        val stiffness = index % 2 == 0
        val close = config.close
        val result = if (!config.customized) {
            if (stiffness && value >= 5f) value * squared else value
        } else {
            val axis = when (index) {
                2, 3 -> close.x
                4, 5 -> if (close.split) close.y else close.x
                else -> if (close.split) close.scale else close.x
            }
            when {
                stiffness && value >= 5f -> max(1f, axis.stiffness * squared)
                !stiffness && value <= 5f -> axis.damping
                else -> value
            }
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
        private const val LAUNCH_DURATION = 500L
        private const val MULTI_VALUE = "com.android.quickstep.util.MultiValueUpdateListener"
        private val CLOSE_RUNNERS = listOf(
            "com.android.quickstep.SwipeUpAnimationLogic\$SpringAnimationRunner",
            "com.android.launcher3.QuickstepTransitionManager\$SpringAnimRunner"
        )
    }
}
