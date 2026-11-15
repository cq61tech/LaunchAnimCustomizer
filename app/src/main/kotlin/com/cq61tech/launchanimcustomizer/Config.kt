package com.cq61tech.launchanimcustomizer

import android.content.SharedPreferences
import androidx.annotation.StringRes

object Keys {
    const val GROUP = "launchanim"
    const val ENABLED = "enabled"
    const val MODE = "mode"
    const val OPEN_STIFFNESS = "open_stiffness"
    const val OPEN_DAMPING = "open_damping"
    const val CLOSE_STIFFNESS = "close_stiffness"
    const val CLOSE_DAMPING = "close_damping"
    const val DEBUG = "debug"
}

object Limits {
    const val STIFFNESS_MIN = 50f
    const val STIFFNESS_MAX = 3000f
    const val DAMPING_MIN = 0.1f
    const val DAMPING_MAX = 1.2f
}

data class Spring(val stiffness: Float, val damping: Float)

data class Preset(
    val id: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val spring: Spring?
)

object Presets {
    const val CLASSIC = "classic"
    const val CUSTOM = "custom"

    val all = listOf(
        Preset(CLASSIC, R.string.preset_classic, R.string.preset_classic_desc, null),
        Preset("quick", R.string.preset_quick, R.string.preset_quick_desc, Spring(1300f, 0.9f)),
        Preset("elegant", R.string.preset_elegant, R.string.preset_elegant_desc, Spring(280f, 0.9f)),
        Preset("bouncy", R.string.preset_bouncy, R.string.preset_bouncy_desc, Spring(550f, 0.55f)),
        Preset("jelly", R.string.preset_jelly, R.string.preset_jelly_desc, Spring(380f, 0.28f)),
        Preset("snappy", R.string.preset_snappy, R.string.preset_snappy_desc, Spring(1600f, 0.8f)),
        Preset("smooth", R.string.preset_smooth, R.string.preset_smooth_desc, Spring(600f, 1f)),
        Preset("instant", R.string.preset_instant, R.string.preset_instant_desc, Spring(3000f, 1f))
    )

    fun find(id: String): Preset? = all.firstOrNull { it.id == id }
}

data class Config(
    val enabled: Boolean = true,
    val mode: String = Presets.CLASSIC,
    val open: Spring = Spring(800f, 0.9f),
    val close: Spring = Spring(800f, 0.9f),
    val debug: Boolean = false
) {
    fun withPreset(preset: Preset): Config {
        val spring = preset.spring ?: return copy(mode = preset.id)
        return copy(mode = preset.id, open = spring, close = spring)
    }

    fun write(prefs: SharedPreferences) {
        prefs.edit()
            .putBoolean(Keys.ENABLED, enabled)
            .putString(Keys.MODE, mode)
            .putFloat(Keys.OPEN_STIFFNESS, open.stiffness)
            .putFloat(Keys.OPEN_DAMPING, open.damping)
            .putFloat(Keys.CLOSE_STIFFNESS, close.stiffness)
            .putFloat(Keys.CLOSE_DAMPING, close.damping)
            .putBoolean(Keys.DEBUG, debug)
            .apply()
    }

    companion object {
        fun read(prefs: SharedPreferences): Config {
            val base = Config()
            return Config(
                enabled = prefs.getBoolean(Keys.ENABLED, base.enabled),
                mode = prefs.getString(Keys.MODE, base.mode) ?: base.mode,
                open = Spring(
                    prefs.getFloat(Keys.OPEN_STIFFNESS, base.open.stiffness),
                    prefs.getFloat(Keys.OPEN_DAMPING, base.open.damping)
                ),
                close = Spring(
                    prefs.getFloat(Keys.CLOSE_STIFFNESS, base.close.stiffness),
                    prefs.getFloat(Keys.CLOSE_DAMPING, base.close.damping)
                ),
                debug = prefs.getBoolean(Keys.DEBUG, base.debug)
            )
        }
    }
}
