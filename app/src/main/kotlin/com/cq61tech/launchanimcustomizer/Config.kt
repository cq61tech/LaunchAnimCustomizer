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
    const val STIFFNESS_MIN = 5f
    const val STIFFNESS_MAX = 10000f
    const val DAMPING_MIN = 0.02f
    const val DAMPING_MAX = 3f
    const val TENSION_MIN = 0.2f
    const val TENSION_MAX = 12f
    const val BEZIER_Y_MIN = -3f
    const val BEZIER_Y_MAX = 4f
    const val DURATION_MIN = 40f
    const val DURATION_MAX = 4000f
    const val ROUNDNESS_MIN = 0f
    const val ROUNDNESS_MAX = 4f
    const val SHADOW_MIN = 0f
    const val SHADOW_MAX = 6f
    const val BLUR_MIN = 0f
    const val BLUR_MAX = 5f
    const val ORIGIN_MIN = -1f
    const val ORIGIN_MAX = 2.5f
    const val LEVEL_MIN = 0f
    const val LEVEL_MAX = 100f
    const val LEVEL_DEFAULT = 50f
}

enum class Curve { SPRING, OVERSHOOT, ANTICIPATE, BOUNCE, DECELERATE, EASE, BEZIER, LINEAR }

data class Spring(val stiffness: Float, val damping: Float)

data class CurveSpec(
    val curve: Curve = Curve.SPRING,
    val spring: Spring = Spring(800f, 0.9f),
    val tension: Float = 2f,
    val x1: Float = 0.2f,
    val y1: Float = 0f,
    val x2: Float = 0f,
    val y2: Float = 1f
)

data class OpenSettings(
    val main: CurveSpec = CurveSpec(),
    val horizontal: CurveSpec? = null,
    val corner: CurveSpec? = null,
    val duration: Float = 500f,
    val roundness: Float = 1f,
    val shadow: Float = 1f,
    val fadeStart: Float = 0.05f,
    val fadeEnd: Float = 0.1f,
    val originX: Float = 1f,
    val originY: Float = 1f
)

data class CloseSettings(
    val x: Spring = Spring(800f, 0.9f),
    val y: Spring = x,
    val scale: Spring = x,
    val split: Boolean = false,
    val roundness: Float = 1f
)

data class Preset(
    val id: String,
    @StringRes val name: Int,
    @StringRes val description: Int,
    val open: OpenSettings?,
    val close: CloseSettings?,
    val blur: Float = 1f
)

data class UserPreset(
    val id: String,
    val name: String,
    val open: OpenSettings,
    val close: CloseSettings,
    val blur: Float = 1f
)

data class Config(
    val enabled: Boolean = true,
    val mode: String = Modes.CLASSIC,
    val open: OpenSettings = OpenSettings(),
    val close: CloseSettings = CloseSettings(),
    val debug: Boolean = false,
    val speed: Float = Limits.LEVEL_DEFAULT,
    val blur: Float = 1f
) {
    val customized: Boolean get() = enabled && mode != Modes.CLASSIC
    val speedActive: Boolean get() = enabled && speed != Limits.LEVEL_DEFAULT
    val blurActive: Boolean get() = enabled && blur != 1f
    val speedFactor: Float get() = Math.pow(2.0, ((speed - Limits.LEVEL_DEFAULT) / 25f).toDouble()).toFloat()

    fun withPreset(preset: Preset): Config {
        val open = preset.open ?: return copy(mode = preset.id, blur = preset.blur)
        val close = preset.close ?: return copy(mode = preset.id, blur = preset.blur)
        return copy(mode = preset.id, open = open, close = close, blur = preset.blur)
    }

    fun withUserPreset(preset: UserPreset): Config = copy(
        mode = Modes.USER_PREFIX + preset.id,
        open = preset.open,
        close = preset.close,
        blur = preset.blur
    )

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
        .put("speed", speed.toDouble())
        .put("blur", blur.toDouble())
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
                    debug = node.optBoolean("debug", false),
                    speed = node.optDouble("speed", Limits.LEVEL_DEFAULT.toDouble()).toFloat(),
                    blur = node.optDouble("blur", 1.0).toFloat()
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

fun CurveSpec.toJson(): JSONObject = JSONObject()
    .put("curve", curve.name)
    .put("spring", spring.toJson())
    .put("tension", tension.toDouble())
    .put("x1", x1.toDouble())
    .put("y1", y1.toDouble())
    .put("x2", x2.toDouble())
    .put("y2", y2.toDouble())

fun OpenSettings.toJson(): JSONObject {
    val node = JSONObject()
        .put("main", main.toJson())
        .put("duration", duration.toDouble())
        .put("roundness", roundness.toDouble())
        .put("shadow", shadow.toDouble())
        .put("fadeStart", fadeStart.toDouble())
        .put("fadeEnd", fadeEnd.toDouble())
        .put("originX", originX.toDouble())
        .put("originY", originY.toDouble())
    horizontal?.let { node.put("horizontal", it.toJson()) }
    corner?.let { node.put("corner", it.toJson()) }
    return node
}

fun CloseSettings.toJson(): JSONObject = JSONObject()
    .put("x", x.toJson())
    .put("y", y.toJson())
    .put("scale", scale.toJson())
    .put("split", split)
    .put("roundness", roundness.toDouble())

fun curveFrom(node: JSONObject?): CurveSpec {
    val base = CurveSpec()
    if (node == null) return base
    val curve = runCatching { Curve.valueOf(node.optString("curve", base.curve.name)) }
        .getOrDefault(base.curve)
    return CurveSpec(
        curve = curve,
        spring = node.springOr("spring", base.spring),
        tension = node.floatOr("tension", base.tension),
        x1 = node.floatOr("x1", base.x1),
        y1 = node.floatOr("y1", base.y1),
        x2 = node.floatOr("x2", base.x2),
        y2 = node.floatOr("y2", base.y2)
    )
}

fun openFrom(node: JSONObject?): OpenSettings {
    val base = OpenSettings()
    if (node == null) return base
    return OpenSettings(
        main = curveFrom(node.optJSONObject("main")),
        horizontal = node.optJSONObject("horizontal")?.let { curveFrom(it) },
        corner = node.optJSONObject("corner")?.let { curveFrom(it) },
        duration = node.floatOr("duration", base.duration),
        roundness = node.floatOr("roundness", base.roundness),
        shadow = node.floatOr("shadow", base.shadow),
        fadeStart = node.floatOr("fadeStart", base.fadeStart),
        fadeEnd = node.floatOr("fadeEnd", base.fadeEnd),
        originX = node.floatOr("originX", base.originX),
        originY = node.floatOr("originY", base.originY)
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
        roundness = node.floatOr("roundness", base.roundness)
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
                .put("blur", it.blur.toDouble())
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
                close = closeFrom(node.optJSONObject("close")),
                blur = node.optDouble("blur", 1.0).toFloat()
            )
        }
    }.getOrDefault(emptyList())
}
