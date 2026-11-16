package com.cq61tech.launchanimcustomizer

import org.json.JSONObject

object DockLimits {
    const val WIDTH_MIN = 0.4f
    const val WIDTH_MAX = 1f
    const val HEIGHT_MIN = 56f
    const val HEIGHT_MAX = 96f
    const val OPACITY_MIN = 0.3f
    const val OPACITY_MAX = 1f
    const val TEXT_MIN = 8f
    const val TEXT_MAX = 14f
    const val BOTTOM_MIN = 0f
    const val BOTTOM_MAX = 48f
}

data class DockSettings(
    val floating: Boolean = true,
    val width: Float = 0.8f,
    val height: Float = 64f,
    val opacity: Float = 1f,
    val textSize: Float = 11f,
    val bottom: Float = 8f,
    val hideOnScroll: Boolean = true,
    val labels: Boolean = true
) {
    fun toRaw(): String = JSONObject()
        .put("floating", floating)
        .put("width", width.toDouble())
        .put("height", height.toDouble())
        .put("opacity", opacity.toDouble())
        .put("textSize", textSize.toDouble())
        .put("bottom", bottom.toDouble())
        .put("hideOnScroll", hideOnScroll)
        .put("labels", labels)
        .toString()

    companion object {
        fun fromRaw(text: String?): DockSettings {
            val base = DockSettings()
            if (text.isNullOrBlank()) return base
            return runCatching {
                val node = JSONObject(text)
                DockSettings(
                    floating = node.optBoolean("floating", base.floating),
                    width = node.optDouble("width", base.width.toDouble()).toFloat()
                        .coerceIn(DockLimits.WIDTH_MIN, DockLimits.WIDTH_MAX),
                    height = node.optDouble("height", base.height.toDouble()).toFloat()
                        .coerceIn(DockLimits.HEIGHT_MIN, DockLimits.HEIGHT_MAX),
                    opacity = node.optDouble("opacity", base.opacity.toDouble()).toFloat()
                        .coerceIn(DockLimits.OPACITY_MIN, DockLimits.OPACITY_MAX),
                    textSize = node.optDouble("textSize", base.textSize.toDouble()).toFloat()
                        .coerceIn(DockLimits.TEXT_MIN, DockLimits.TEXT_MAX),
                    bottom = node.optDouble("bottom", base.bottom.toDouble()).toFloat()
                        .coerceIn(DockLimits.BOTTOM_MIN, DockLimits.BOTTOM_MAX),
                    hideOnScroll = node.optBoolean("hideOnScroll", base.hideOnScroll),
                    labels = node.optBoolean("labels", base.labels)
                )
            }.getOrDefault(base)
        }
    }
}
