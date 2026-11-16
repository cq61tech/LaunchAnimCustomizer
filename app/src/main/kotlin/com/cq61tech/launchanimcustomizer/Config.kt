package com.cq61tech.launchanimcustomizer

import androidx.annotation.StringRes
import org.json.JSONArray
import org.json.JSONObject

object Keys {
    const val GROUP = "launchanim"
    const val CONFIG = "config"
    const val USER_PRESETS = "user_presets"
    const val DOCK = "dock"
}

object Modes {
    const val CLASSIC = "classic"
    const val CUSTOM = "custom"
    const val USER_PREFIX = "user:"
}

object Limits {
    const val STIFFNESS_MIN = 20f
    const val STIFFNESS_MAX = 5000f
    const val DAMPING_MIN = 0.05f
    const val DAMPING_MAX = 2f
    const val SPEED_MIN = 0.25f
    const val SPEED_MAX = 4f
    const val TENSION_MIN = 0.2f
    const val TENSION_MAX = 10f
    const val BEZIER_Y_MIN = -2f
    const val BEZIER_Y_MAX = 3f
}

enum class Curve { SPRING, OVERSHOOT, ANTICIPATE, BOUNCE, DECELERATE, EASE, BEZIER, LINEAR }

data class Spring(val stiffness: Float, val damping: Float)

data class OpenSettings(
    val curve: Curve = Curve.SPRING,
    val spring: Spring = Spring(800f, 0.9f),
    val tension: Float = 2f,
    val x1: Float = 0.2f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 1f,
    val speed: Float = 1f
)

data class CloseSettings(
    val x: Spring = Spring(800f, 0.9f),
    val y: Spring = x,
    val scale: Spring = x,
    val split: Boolean = false,
    val speed: Float = 1f
)

data class Preset(
    val id: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val open: OpenSettings?,
    val close: CloseSettings?
)

data class UserPreset(
    val id: String,
    val name: String,
    val open: OpenSettings,
    val close: CloseSettings
)

data class Config(
    val enabled: Boolean = true,
    val mode: String = Modes.CLASSIC,
    val open: OpenSettings = OpenSettings(),
    val close: CloseSettings = CloseSettings(),
    val debug: Boolean = false
) {
    val active: Boolean get() = enabled && mode != Modes.CLASSIC

    fun withPreset(preset: Preset): Config {
        val open = preset.open ?: return copy(mode = preset.id)
        val close = preset.close ?: return copy(mode = preset.id)
        return copy(mode = preset.id, open = open, close = close)
    }

    fun withUserPreset(preset: UserPreset): Config =
        copy(mode = Modes.USER_PREFIX + preset.id, open = preset.open, close = preset.close)

    fun editOpen(change: (OpenSettings) -> OpenSettings): Config =
        copy(mode = Modes.CUSTOM, open = change(open))

    fun editClose(change: (CloseSettings) -> CloseSettings): Config =
        copy(mode = Modes.CUSTOM, close = change(close))

    fun toRaw(): String = JSONObject()
        .put("enabled", enabled)
        .put("mode", mode)
        .put("open", open.toJson())
        .put("close", close.toJson())
        .put("debug", debug)
        .toString()

    companion object {
        fun parse(text: String?): Config? {
            if (text.isNullOrBlank()) return null
            return runCatching {
                val node = JSONObject(text)
                Config(
                    enabled = node.optBoolean("enabled", true),
                    mode = node.optString("mode", Modes.CLASSIC),
                    open = openFrom(node.optJSONObject("open")),
                    close = closeFrom(node.optJSONObject("close")),
                    debug = node.optBoolean("debug", false)
                )
            }.getOrNull()
        }

        fun fromRaw(text: String?): Config = parse(text) ?: Config()
    }
}

private fun Spring.toJson(): JSONObject =
    JSONObject().put("s", stiffness.toDouble()).put("d", damping.toDouble())

private fun JSONObject.springOr(name: String, base: Spring): Spring {
    val node = optJSONObject(name) ?: return base
    return Spring(
        node.optDouble("s", base.stiffness.toDouble()).toFloat(),
        node.optDouble("d", base.damping.toDouble()).toFloat()
    )
}

private fun JSONObject.floatOr(name: String, base: Float): Float =
    optDouble(name, base.toDouble()).toFloat()

fun OpenSettings.toJson(): JSONObject = JSONObject()
    .put("curve", curve.name)
    .put("spring", spring.toJson())
    .put("tension", tension.toDouble())
    .put("x1", x1.toDouble())
    .put("y1", y1.toDouble())
    .put("x2", x2.toDouble())
    .put("y2", y2.toDouble())
    .put("speed", speed.toDouble())

fun CloseSettings.toJson(): JSONObject = JSONObject()
    .put("x", x.toJson())
    .put("y", y.toJson())
    .put("scale", scale.toJson())
    .put("split", split)
    .put("speed", speed.toDouble())

fun openFrom(node: JSONObject?): OpenSettings {
    val base = OpenSettings()
    if (node == null) return base
    val curve = runCatching { Curve.valueOf(node.optString("curve", base.curve.name)) }
        .getOrDefault(base.curve)
    return OpenSettings(
        curve = curve,
        spring = node.springOr("spring", base.spring),
        tension = node.floatOr("tension", base.tension),
        x1 = node.floatOr("x1", base.x1),
        y1 = node.floatOr("y1", base.y1),
        x2 = node.floatOr("x2", base.x2),
        y2 = node.floatOr("y2", base.y2),
        speed = node.floatOr("speed", base.speed)
    )
}

fun closeFrom(node: JSONObject?): CloseSettings {
    val base = CloseSettings()
    if (node == null) return base
    val x = node.springOr("x", base.x)
    return CloseSettings(
        x = x,
        y = node.springOr("y", x),
        scale = node.springOr("scale", x),
        split = node.optBoolean("split", false),
        speed = node.floatOr("speed", base.speed)
    )
}

fun userPresetsToRaw(list: List<UserPreset>): String {
    val array = JSONArray()
    list.forEach {
        array.put(
            JSONObject()
                .put("id", it.id)
                .put("name", it.name)
                .put("open", it.open.toJson())
                .put("close", it.close.toJson())
        )
    }
    return array.toString()
}

fun userPresetsFromRaw(text: String?): List<UserPreset> {
    if (text.isNullOrBlank()) return emptyList()
    return runCatching {
        val array = JSONArray(text)
        (0 until array.length()).map {
            val node = array.getJSONObject(it)
            UserPreset(
                id = node.getString("id"),
                name = node.getString("name"),
                open = openFrom(node.optJSONObject("open")),
                close = closeFrom(node.optJSONObject("close"))
            )
        }
    }.getOrDefault(emptyList())
}
