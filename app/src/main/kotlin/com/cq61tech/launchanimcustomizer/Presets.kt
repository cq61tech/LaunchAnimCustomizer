package com.cq61tech.launchanimcustomizer

object Presets {
    private fun spring(stiffness: Float, damping: Float) =
        CurveSpec(Curve.SPRING, Spring(stiffness, damping))

    private fun shape(curve: Curve, tension: Float = 2f) = CurveSpec(curve, tension = tension)

    private fun bezier(x1: Float, y1: Float, x2: Float, y2: Float) =
        CurveSpec(Curve.BEZIER, x1 = x1, y1 = y1, x2 = x2, y2 = y2)

    private fun spring2(stiffness: Float, damping: Float) = Spring(stiffness, damping)

    val all: List<Preset> = listOf(
        Preset("classic", R.string.preset_classic, R.string.preset_classic_desc, null, null),
        Preset(
            "quick",
            R.string.preset_quick,
            R.string.preset_quick_desc,
            OpenSettings(main = spring(1300.0f, 0.9f), duration = 260.0f),
            CloseSettings(x = spring2(1300.0f, 0.9f), speed = 1.2f)
        ),
        Preset(
            "elegant",
            R.string.preset_elegant,
            R.string.preset_elegant_desc,
            OpenSettings(main = spring(280.0f, 0.9f), duration = 620.0f, roundness = 1.3f),
            CloseSettings(x = spring2(260.0f, 0.95f), roundness = 1.3f)
        ),
        Preset(
            "bouncy",
            R.string.preset_bouncy,
            R.string.preset_bouncy_desc,
            OpenSettings(main = spring(550.0f, 0.55f), duration = 560.0f),
            CloseSettings(x = spring2(520.0f, 0.5f))
        ),
        Preset(
            "jelly",
            R.string.preset_jelly,
            R.string.preset_jelly_desc,
            OpenSettings(main = spring(380.0f, 0.28f), duration = 820.0f, horizontal = spring(300.0f, 0.22f), roundness = 1.6f),
            CloseSettings(x = spring2(260.0f, 0.25f), y = spring2(420.0f, 0.3f), scale = spring2(360.0f, 0.28f), split = true, roundness = 1.6f)
        ),
        Preset(
            "snappy",
            R.string.preset_snappy,
            R.string.preset_snappy_desc,
            OpenSettings(main = spring(1600.0f, 0.8f), duration = 240.0f, roundness = 0.8f),
            CloseSettings(x = spring2(1700.0f, 0.8f), roundness = 0.8f)
        ),
        Preset(
            "smooth",
            R.string.preset_smooth,
            R.string.preset_smooth_desc,
            OpenSettings(main = spring(600.0f, 1.0f), duration = 520.0f),
            CloseSettings(x = spring2(600.0f, 1.0f))
        ),
        Preset(
            "instant",
            R.string.preset_instant,
            R.string.preset_instant_desc,
            OpenSettings(main = shape(Curve.LINEAR), duration = 90.0f),
            CloseSettings(x = spring2(4000.0f, 1.0f))
        ),
        Preset(
            "rubber",
            R.string.preset_rubber,
            R.string.preset_rubber_desc,
            OpenSettings(main = spring(260.0f, 0.18f), duration = 900.0f, roundness = 2.0f),
            CloseSettings(x = spring2(240.0f, 0.16f), roundness = 2.0f)
        ),
        Preset(
            "gentle",
            R.string.preset_gentle,
            R.string.preset_gentle_desc,
            OpenSettings(main = shape(Curve.EASE), duration = 780.0f, roundness = 1.2f),
            CloseSettings(x = spring2(160.0f, 1.0f))
        ),
        Preset(
            "heavy",
            R.string.preset_heavy,
            R.string.preset_heavy_desc,
            OpenSettings(main = spring(120.0f, 0.75f), duration = 700.0f, roundness = 0.6f, shadow = 2.0f),
            CloseSettings(x = spring2(110.0f, 0.7f), roundness = 0.6f)
        ),
        Preset(
            "wobble",
            R.string.preset_wobble,
            R.string.preset_wobble_desc,
            OpenSettings(main = spring(500.0f, 0.35f), duration = 650.0f, horizontal = shape(Curve.OVERSHOOT, 3.0f)),
            CloseSettings(x = spring2(500.0f, 0.6f), y = spring2(500.0f, 0.3f), scale = spring2(500.0f, 0.45f), split = true)
        ),
        Preset(
            "liquid",
            R.string.preset_liquid,
            R.string.preset_liquid_desc,
            OpenSettings(main = spring(320.0f, 0.5f), duration = 760.0f, corner = spring(200.0f, 0.3f), roundness = 1.8f),
            CloseSettings(x = spring2(300.0f, 0.45f), roundness = 1.8f)
        ),
        Preset(
            "turbo",
            R.string.preset_turbo,
            R.string.preset_turbo_desc,
            OpenSettings(main = spring(2200.0f, 0.9f), duration = 180.0f, roundness = 0.5f),
            CloseSettings(x = spring2(2400.0f, 0.9f), speed = 1.3f, roundness = 0.5f)
        ),
        Preset(
            "pop",
            R.string.preset_pop,
            R.string.preset_pop_desc,
            OpenSettings(main = shape(Curve.OVERSHOOT, 3.5f), duration = 380.0f, fadeStart = 0.0f, fadeEnd = 0.05f),
            CloseSettings(x = spring2(900.0f, 0.6f))
        ),
        Preset(
            "glide",
            R.string.preset_glide,
            R.string.preset_glide_desc,
            OpenSettings(main = shape(Curve.DECELERATE, 3.0f), duration = 900.0f, roundness = 1.4f),
            CloseSettings(x = spring2(450.0f, 1.0f))
        ),
        Preset(
            "anticipate",
            R.string.preset_anticipate,
            R.string.preset_anticipate_desc,
            OpenSettings(main = shape(Curve.ANTICIPATE, 2.5f), duration = 640.0f),
            CloseSettings(x = spring2(700.0f, 0.65f))
        ),
        Preset(
            "bounce",
            R.string.preset_bounce,
            R.string.preset_bounce_desc,
            OpenSettings(main = shape(Curve.BOUNCE), duration = 760.0f, horizontal = shape(Curve.DECELERATE, 2.0f)),
            CloseSettings(x = spring2(450.0f, 0.4f))
        ),
        Preset(
            "cinematic",
            R.string.preset_cinematic,
            R.string.preset_cinematic_desc,
            OpenSettings(main = bezier(0.05f, 0.7f, 0.1f, 1.0f), duration = 1100.0f, roundness = 1.5f, shadow = 2.5f, fadeStart = 0.25f, fadeEnd = 0.6f),
            CloseSettings(x = spring2(200.0f, 0.95f), roundness = 1.5f)
        ),
        Preset(
            "linear",
            R.string.preset_linear,
            R.string.preset_linear_desc,
            OpenSettings(main = shape(Curve.LINEAR), duration = 450.0f, roundness = 0.0f),
            CloseSettings(x = spring2(1000.0f, 1.0f), roundness = 0.0f)
        ),
        Preset(
            "arc",
            R.string.preset_arc,
            R.string.preset_arc_desc,
            OpenSettings(main = spring(700.0f, 0.8f), duration = 560.0f, horizontal = spring(260.0f, 1.0f)),
            CloseSettings(x = spring2(700.0f, 0.8f), y = spring2(520.0f, 0.7f), scale = spring2(600.0f, 0.75f), split = true)
        ),
        Preset(
            "swoop",
            R.string.preset_swoop,
            R.string.preset_swoop_desc,
            OpenSettings(main = shape(Curve.DECELERATE, 3.5f), duration = 700.0f, horizontal = shape(Curve.EASE), roundness = 1.2f),
            CloseSettings(x = spring2(500.0f, 0.9f), y = spring2(300.0f, 0.9f), scale = spring2(420.0f, 0.9f), split = true)
        ),
        Preset(
            "elastic",
            R.string.preset_elastic,
            R.string.preset_elastic_desc,
            OpenSettings(main = spring(220.0f, 0.15f), duration = 840.0f, roundness = 1.4f),
            CloseSettings(x = spring2(200.0f, 0.14f), roundness = 1.4f)
        ),
        Preset(
            "slingshot",
            R.string.preset_slingshot,
            R.string.preset_slingshot_desc,
            OpenSettings(main = shape(Curve.ANTICIPATE, 4.0f), duration = 520.0f, horizontal = shape(Curve.OVERSHOOT, 2.0f)),
            CloseSettings(x = spring2(1200.0f, 0.5f))
        ),
        Preset(
            "morph",
            R.string.preset_morph,
            R.string.preset_morph_desc,
            OpenSettings(main = spring(420.0f, 0.7f), duration = 700.0f, corner = shape(Curve.OVERSHOOT, 4.0f), roundness = 2.2f),
            CloseSettings(x = spring2(420.0f, 0.7f), roundness = 2.2f)
        ),
        Preset(
            "sharp",
            R.string.preset_sharp,
            R.string.preset_sharp_desc,
            OpenSettings(main = bezier(0.3f, 0.0f, 0.0f, 1.0f), duration = 300.0f, roundness = 0.15f, shadow = 0.0f),
            CloseSettings(x = spring2(1400.0f, 0.9f), roundness = 0.15f)
        ),
        Preset(
            "soft",
            R.string.preset_soft,
            R.string.preset_soft_desc,
            OpenSettings(main = shape(Curve.EASE), duration = 600.0f, roundness = 2.5f, shadow = 0.3f),
            CloseSettings(x = spring2(500.0f, 1.0f), roundness = 2.5f)
        ),
        Preset(
            "lift",
            R.string.preset_lift,
            R.string.preset_lift_desc,
            OpenSettings(main = spring(500.0f, 0.7f), duration = 520.0f, roundness = 1.1f, shadow = 3.0f),
            CloseSettings(x = spring2(520.0f, 0.7f))
        ),
        Preset(
            "drift",
            R.string.preset_drift,
            R.string.preset_drift_desc,
            OpenSettings(main = spring(90.0f, 1.0f), duration = 1300.0f, roundness = 1.3f),
            CloseSettings(x = spring2(90.0f, 1.0f), roundness = 1.3f)
        ),
        Preset(
            "whip",
            R.string.preset_whip,
            R.string.preset_whip_desc,
            OpenSettings(main = shape(Curve.OVERSHOOT, 5.0f), duration = 220.0f),
            CloseSettings(x = spring2(1800.0f, 0.45f))
        ),
        Preset(
            "dream",
            R.string.preset_dream,
            R.string.preset_dream_desc,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 1000.0f, horizontal = bezier(0.6f, 0.0f, 0.4f, 1.0f), roundness = 1.6f, fadeStart = 0.1f, fadeEnd = 0.4f),
            CloseSettings(x = spring2(140.0f, 1.0f), roundness = 1.6f)
        )
    )

    fun find(id: String): Preset? = all.firstOrNull { it.id == id }
}
