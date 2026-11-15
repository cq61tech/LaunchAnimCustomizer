package com.cq61tech.launchanimcustomizer

object Presets {
    val all: List<Preset> = listOf(
        Preset("classic", R.string.preset_classic, R.string.preset_classic_desc, null, null),
        Preset(
            "quick",
            R.string.preset_quick,
            R.string.preset_quick_desc,
            OpenSettings(spring = Spring(1300.0f, 0.9f)),
            CloseSettings(Spring(1300.0f, 0.9f))
        ),
        Preset(
            "elegant",
            R.string.preset_elegant,
            R.string.preset_elegant_desc,
            OpenSettings(spring = Spring(280.0f, 0.9f)),
            CloseSettings(Spring(280.0f, 0.9f))
        ),
        Preset(
            "bouncy",
            R.string.preset_bouncy,
            R.string.preset_bouncy_desc,
            OpenSettings(spring = Spring(550.0f, 0.55f)),
            CloseSettings(Spring(550.0f, 0.55f))
        ),
        Preset(
            "jelly",
            R.string.preset_jelly,
            R.string.preset_jelly_desc,
            OpenSettings(spring = Spring(380.0f, 0.28f)),
            CloseSettings(Spring(380.0f, 0.28f))
        ),
        Preset(
            "snappy",
            R.string.preset_snappy,
            R.string.preset_snappy_desc,
            OpenSettings(spring = Spring(1600.0f, 0.8f)),
            CloseSettings(Spring(1600.0f, 0.8f))
        ),
        Preset(
            "smooth",
            R.string.preset_smooth,
            R.string.preset_smooth_desc,
            OpenSettings(spring = Spring(600.0f, 1.0f)),
            CloseSettings(Spring(600.0f, 1.0f))
        ),
        Preset(
            "instant",
            R.string.preset_instant,
            R.string.preset_instant_desc,
            OpenSettings(spring = Spring(3000.0f, 1.0f)),
            CloseSettings(Spring(3000.0f, 1.0f))
        ),
        Preset(
            "rubber",
            R.string.preset_rubber,
            R.string.preset_rubber_desc,
            OpenSettings(spring = Spring(260.0f, 0.18f)),
            CloseSettings(Spring(260.0f, 0.18f))
        ),
        Preset(
            "gentle",
            R.string.preset_gentle,
            R.string.preset_gentle_desc,
            OpenSettings(spring = Spring(180.0f, 1.0f)),
            CloseSettings(Spring(180.0f, 1.0f))
        ),
        Preset(
            "heavy",
            R.string.preset_heavy,
            R.string.preset_heavy_desc,
            OpenSettings(spring = Spring(120.0f, 0.75f)),
            CloseSettings(Spring(120.0f, 0.75f))
        ),
        Preset(
            "wobble",
            R.string.preset_wobble,
            R.string.preset_wobble_desc,
            OpenSettings(spring = Spring(500.0f, 0.35f)),
            CloseSettings(Spring(500.0f, 0.35f))
        ),
        Preset(
            "liquid",
            R.string.preset_liquid,
            R.string.preset_liquid_desc,
            OpenSettings(spring = Spring(320.0f, 0.5f)),
            CloseSettings(Spring(320.0f, 0.5f))
        ),
        Preset(
            "turbo",
            R.string.preset_turbo,
            R.string.preset_turbo_desc,
            OpenSettings(spring = Spring(2200.0f, 0.9f)),
            CloseSettings(Spring(2200.0f, 0.9f))
        ),
        Preset(
            "pop",
            R.string.preset_pop,
            R.string.preset_pop_desc,
            OpenSettings(curve = Curve.OVERSHOOT, tension = 3.5f),
            CloseSettings(Spring(900.0f, 0.6f))
        ),
        Preset(
            "glide",
            R.string.preset_glide,
            R.string.preset_glide_desc,
            OpenSettings(curve = Curve.DECELERATE, tension = 3f),
            CloseSettings(Spring(500.0f, 1.0f))
        ),
        Preset(
            "anticipate",
            R.string.preset_anticipate,
            R.string.preset_anticipate_desc,
            OpenSettings(curve = Curve.ANTICIPATE, tension = 2.5f),
            CloseSettings(Spring(700.0f, 0.65f))
        ),
        Preset(
            "bounce",
            R.string.preset_bounce,
            R.string.preset_bounce_desc,
            OpenSettings(curve = Curve.BOUNCE),
            CloseSettings(Spring(450.0f, 0.4f))
        ),
        Preset(
            "cinematic",
            R.string.preset_cinematic,
            R.string.preset_cinematic_desc,
            OpenSettings(curve = Curve.BEZIER, x1 = 0.05f, y1 = 0.7f, x2 = 0.1f, y2 = 1f),
            CloseSettings(Spring(220.0f, 0.95f))
        ),
        Preset(
            "linear",
            R.string.preset_linear,
            R.string.preset_linear_desc,
            OpenSettings(curve = Curve.LINEAR),
            CloseSettings(Spring(1000.0f, 1.0f))
        )
    )

    fun find(id: String): Preset? = all.firstOrNull { it.id == id }
}
