package com.cq61tech.launchanimcustomizer

object Presets {
    private fun spring(stiffness: Float, damping: Float) =
        CurveSpec(Curve.SPRING, Spring(stiffness, damping))

    private fun shape(curve: Curve, tension: Float = 2f) = CurveSpec(curve, tension = tension)

    private fun bezier(x1: Float, y1: Float, x2: Float, y2: Float) =
        CurveSpec(Curve.BEZIER, x1 = x1, y1 = y1, x2 = x2, y2 = y2)

    val all: List<Preset> = listOf(
        Preset("classic", R.string.preset_classic, R.string.preset_classic_desc, null, null),
        Preset(
            "instant",
            R.string.preset_instant,
            R.string.preset_instant_desc,
            OpenSettings(main = shape(Curve.LINEAR), duration = 60.0f, shadow = 0.0f),
            CloseSettings(x = Spring(9000.0f, 1.0f)),
            blur = 0.0f
        ),
        Preset(
            "snappy",
            R.string.preset_snappy,
            R.string.preset_snappy_desc,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 170.0f, roundness = 0.7f, shadow = 0.3f),
            CloseSettings(x = Spring(2600.0f, 0.85f), roundness = 0.7f),
            blur = 0.5f
        ),
        Preset(
            "bouncy",
            R.string.preset_bouncy,
            R.string.preset_bouncy_desc,
            OpenSettings(main = spring(480.0f, 0.42f), duration = 620.0f),
            CloseSettings(x = Spring(450.0f, 0.38f))
        ),
        Preset(
            "jelly",
            R.string.preset_jelly,
            R.string.preset_jelly_desc,
            OpenSettings(main = spring(110.0f, 0.13f), duration = 1400.0f, horizontal = spring(80.0f, 0.1f), corner = spring(140.0f, 0.2f), roundness = 2.6f, shadow = 1.5f),
            CloseSettings(x = Spring(120.0f, 0.14f), y = Spring(200.0f, 0.18f), scale = Spring(150.0f, 0.16f), split = true, roundness = 2.6f),
            blur = 1.5f
        ),
        Preset(
            "pop",
            R.string.preset_pop,
            R.string.preset_pop_desc,
            OpenSettings(main = shape(Curve.OVERSHOOT, 6.0f), duration = 240.0f, fadeStart = 0.0f, fadeEnd = 0.04f),
            CloseSettings(x = Spring(1100.0f, 0.4f)),
            blur = 0.0f
        ),
        Preset(
            "anticipate",
            R.string.preset_anticipate,
            R.string.preset_anticipate_desc,
            OpenSettings(main = shape(Curve.ANTICIPATE, 5.0f), duration = 800.0f),
            CloseSettings(x = Spring(500.0f, 0.55f)),
            blur = 1.5f
        ),
        Preset(
            "bounce",
            R.string.preset_bounce,
            R.string.preset_bounce_desc,
            OpenSettings(main = shape(Curve.BOUNCE), duration = 1000.0f, horizontal = shape(Curve.DECELERATE, 3.0f)),
            CloseSettings(x = Spring(380.0f, 0.28f))
        ),
        Preset(
            "cinematic",
            R.string.preset_cinematic,
            R.string.preset_cinematic_desc,
            OpenSettings(main = bezier(0.7f, 0.0f, 0.3f, 1.0f), duration = 1500.0f, roundness = 1.8f, shadow = 3.0f, fadeStart = 0.3f, fadeEnd = 0.8f),
            CloseSettings(x = Spring(170.0f, 0.95f), roundness = 1.8f),
            blur = 3.5f
        ),
        Preset(
            "linear",
            R.string.preset_linear,
            R.string.preset_linear_desc,
            OpenSettings(main = shape(Curve.LINEAR), duration = 600.0f, roundness = 0.0f, shadow = 0.0f),
            CloseSettings(x = Spring(800.0f, 1.0f), roundness = 0.0f),
            blur = 0.0f
        ),
        Preset(
            "sharp",
            R.string.preset_sharp,
            R.string.preset_sharp_desc,
            OpenSettings(main = bezier(0.4f, 0.0f, 0.0f, 1.0f), duration = 200.0f, roundness = 0.05f, shadow = 0.0f),
            CloseSettings(x = Spring(2000.0f, 0.9f), roundness = 0.05f),
            blur = 0.0f
        ),
        Preset(
            "soft",
            R.string.preset_soft,
            R.string.preset_soft_desc,
            OpenSettings(main = shape(Curve.EASE), duration = 800.0f, roundness = 3.2f, shadow = 0.2f),
            CloseSettings(x = Spring(260.0f, 1.0f), roundness = 3.2f),
            blur = 1.8f
        ),
        Preset(
            "lift",
            R.string.preset_lift,
            R.string.preset_lift_desc,
            OpenSettings(main = spring(420.0f, 0.72f), duration = 560.0f, roundness = 1.1f, shadow = 5.0f),
            CloseSettings(x = Spring(420.0f, 0.7f)),
            blur = 2.5f
        ),
        Preset(
            "drift",
            R.string.preset_drift,
            R.string.preset_drift_desc,
            OpenSettings(main = spring(55.0f, 1.0f), duration = 1800.0f, roundness = 1.3f),
            CloseSettings(x = Spring(55.0f, 1.0f), roundness = 1.3f),
            blur = 2.0f
        ),
        Preset(
            "dream",
            R.string.preset_dream,
            R.string.preset_dream_desc,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 1300.0f, horizontal = bezier(0.65f, 0.0f, 0.35f, 1.0f), roundness = 2.0f, fadeStart = 0.1f, fadeEnd = 0.5f),
            CloseSettings(x = Spring(110.0f, 1.0f), roundness = 2.0f),
            blur = 4.5f
        ),
        Preset(
            "arc",
            R.string.preset_arc,
            R.string.preset_arc_desc,
            OpenSettings(main = spring(900.0f, 0.8f), duration = 600.0f, horizontal = spring(120.0f, 0.95f)),
            CloseSettings(x = Spring(150.0f, 0.95f), y = Spring(900.0f, 0.8f), scale = Spring(500.0f, 0.8f), split = true)
        ),
        Preset(
            "whip",
            R.string.preset_whip,
            R.string.preset_whip_desc,
            OpenSettings(main = shape(Curve.OVERSHOOT, 8.0f), duration = 200.0f),
            CloseSettings(x = Spring(2200.0f, 0.3f)),
            blur = 0.0f
        ),
        Preset(
            "bloom",
            R.string.preset_bloom,
            R.string.preset_bloom_desc,
            OpenSettings(main = spring(380.0f, 0.8f), duration = 750.0f, roundness = 2.0f, fadeStart = 0.3f, fadeEnd = 0.7f, originX = 0.0f, originY = 0.0f),
            CloseSettings(x = Spring(380.0f, 0.8f), roundness = 2.0f),
            blur = 2.5f
        ),
        Preset(
            "glass",
            R.string.preset_glass,
            R.string.preset_glass_desc,
            OpenSettings(main = shape(Curve.EASE), duration = 1000.0f, roundness = 1.6f, shadow = 0.15f, fadeStart = 0.15f, fadeEnd = 0.55f),
            CloseSettings(x = Spring(200.0f, 1.0f)),
            blur = 5.0f
        )
    )

    fun find(id: String): Preset? = all.firstOrNull { it.id == id }
}
