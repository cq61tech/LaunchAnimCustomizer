package com.cq61tech.launchanimcustomizer

object Presets {
    private fun spring(stiffness: Float, damping: Float) =
        CurveSpec(Curve.SPRING, Spring(stiffness, damping))

    private fun shape(curve: Curve, tension: Float = 2f, aux: Float = 0.5f) =
        CurveSpec(curve, tension = tension, aux = aux)

    private fun bezier(x1: Float, y1: Float, x2: Float, y2: Float) =
        CurveSpec(Curve.BEZIER, x1 = x1, y1 = y1, x2 = x2, y2 = y2)

    val all: List<Preset> = listOf(
        Preset("classic", "Classic", Category.SYSTEM, null, null),
        Preset(
            "pixel",
            "Pixel",
            Category.SYSTEM,
            OpenSettings(main = bezier(0.05f, 0.7f, 0.1f, 1.0f), duration = 500.0f),
            CloseSettings(x = Spring(700.0f, 0.9f))
        ),
        Preset(
            "oneui",
            "One UI",
            Category.SYSTEM,
            OpenSettings(main = spring(380.0f, 0.88f), duration = 480.0f, roundness = 1.4f, fadeStart = 0.1f, fadeEnd = 0.4f),
            CloseSettings(x = Spring(330.0f, 0.9f), roundness = 1.4f),
            blur = 1.5f
        ),
        Preset(
            "ios",
            "iOS",
            Category.SYSTEM,
            OpenSettings(main = spring(300.0f, 0.78f), duration = 650.0f, roundness = 2.2f, shadow = 0.6f, fadeStart = 0.2f, fadeEnd = 0.6f),
            CloseSettings(x = Spring(260.0f, 0.8f), y = Spring(300.0f, 0.85f), scale = Spring(240.0f, 0.78f), split = true, roundness = 2.2f),
            blur = 2.2f
        ),
        Preset(
            "material",
            "Material",
            Category.SYSTEM,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 420.0f, roundness = 1.2f),
            CloseSettings(x = Spring(500.0f, 1.0f), roundness = 1.2f)
        ),
        Preset(
            "zen",
            "Zen",
            Category.SYSTEM,
            OpenSettings(main = shape(Curve.SMOOTH, 2.0f), duration = 700.0f, roundness = 1.6f, shadow = 0.5f),
            CloseSettings(x = Spring(240.0f, 1.0f))
        ),
        Preset(
            "instant",
            "Instant",
            Category.FAST,
            OpenSettings(main = shape(Curve.LINEAR), duration = 60.0f, shadow = 0.0f),
            CloseSettings(x = Spring(9000.0f, 1.0f)),
            blur = 0.0f
        ),
        Preset(
            "snappy",
            "Snappy",
            Category.FAST,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 170.0f, roundness = 0.7f, shadow = 0.3f),
            CloseSettings(x = Spring(2600.0f, 0.85f), roundness = 0.7f),
            blur = 0.5f
        ),
        Preset(
            "sharp",
            "Sharp",
            Category.FAST,
            OpenSettings(main = bezier(0.4f, 0.0f, 0.0f, 1.0f), duration = 200.0f, roundness = 0.05f, shadow = 0.0f),
            CloseSettings(x = Spring(2000.0f, 0.9f), roundness = 0.05f),
            blur = 0.0f
        ),
        Preset(
            "turbo",
            "Turbo",
            Category.FAST,
            OpenSettings(main = shape(Curve.DECELERATE, 5.0f), duration = 140.0f, roundness = 0.5f),
            CloseSettings(x = Spring(3500.0f, 0.9f), roundness = 0.5f),
            blur = 0.0f
        ),
        Preset(
            "linear",
            "Linear",
            Category.FAST,
            OpenSettings(main = shape(Curve.LINEAR), duration = 380.0f, roundness = 0.0f, shadow = 0.0f),
            CloseSettings(x = Spring(800.0f, 1.0f), roundness = 0.0f),
            blur = 0.0f
        ),
        Preset(
            "rush",
            "Rush",
            Category.FAST,
            OpenSettings(main = shape(Curve.ACCELERATE, 3.0f), duration = 200.0f),
            CloseSettings(x = Spring(1500.0f, 0.8f)),
            blur = 0.5f
        ),
        Preset(
            "bouncy",
            "Bouncy",
            Category.SPRINGY,
            OpenSettings(main = spring(480.0f, 0.42f), duration = 620.0f),
            CloseSettings(x = Spring(450.0f, 0.38f))
        ),
        Preset(
            "jelly",
            "Jelly",
            Category.SPRINGY,
            OpenSettings(main = spring(110.0f, 0.13f), duration = 1400.0f, horizontal = spring(80.0f, 0.1f), corner = spring(140.0f, 0.2f), roundness = 2.6f, shadow = 1.5f),
            CloseSettings(x = Spring(120.0f, 0.14f), y = Spring(200.0f, 0.18f), scale = Spring(150.0f, 0.16f), split = true, roundness = 2.6f),
            blur = 1.5f
        ),
        Preset(
            "rubber",
            "Rubber",
            Category.SPRINGY,
            OpenSettings(main = shape(Curve.ELASTIC, 3.0f, aux = 3.0f), duration = 900.0f, roundness = 2.0f),
            CloseSettings(x = Spring(200.0f, 0.2f), roundness = 2.0f)
        ),
        Preset(
            "wobble",
            "Wobble",
            Category.SPRINGY,
            OpenSettings(main = spring(500.0f, 0.35f), duration = 700.0f, horizontal = shape(Curve.OVERSHOOT, 3.0f)),
            CloseSettings(x = Spring(500.0f, 0.6f), y = Spring(500.0f, 0.3f), scale = Spring(500.0f, 0.45f), split = true)
        ),
        Preset(
            "pendulum",
            "Pendulum",
            Category.SPRINGY,
            OpenSettings(main = shape(Curve.ELASTIC, 5.0f, aux = 5.0f), duration = 1000.0f, roundness = 1.3f),
            CloseSettings(x = Spring(260.0f, 0.15f), y = Spring(260.0f, 0.3f), scale = Spring(260.0f, 0.2f), split = true)
        ),
        Preset(
            "slingshot",
            "Slingshot",
            Category.SPRINGY,
            OpenSettings(main = shape(Curve.ANTICIPATE, 5.0f), duration = 700.0f, horizontal = shape(Curve.OVERSHOOT, 2.0f)),
            CloseSettings(x = Spring(1200.0f, 0.5f))
        ),
        Preset(
            "cinematic",
            "Cinematic",
            Category.CINEMATIC,
            OpenSettings(main = bezier(0.7f, 0.0f, 0.3f, 1.0f), duration = 1500.0f, roundness = 1.8f, shadow = 3.0f, fadeStart = 0.3f, fadeEnd = 0.8f),
            CloseSettings(x = Spring(170.0f, 0.95f), roundness = 1.8f),
            blur = 3.5f
        ),
        Preset(
            "dream",
            "Dream",
            Category.CINEMATIC,
            OpenSettings(main = bezier(0.2f, 0.0f, 0.0f, 1.0f), duration = 1300.0f, horizontal = bezier(0.65f, 0.0f, 0.35f, 1.0f), roundness = 2.0f, fadeStart = 0.1f, fadeEnd = 0.5f),
            CloseSettings(x = Spring(110.0f, 1.0f), roundness = 2.0f),
            blur = 4.5f
        ),
        Preset(
            "drift",
            "Drift",
            Category.CINEMATIC,
            OpenSettings(main = spring(55.0f, 1.0f), duration = 1800.0f, roundness = 1.3f),
            CloseSettings(x = Spring(55.0f, 1.0f), roundness = 1.3f),
            blur = 2.0f
        ),
        Preset(
            "glass",
            "Glass",
            Category.CINEMATIC,
            OpenSettings(main = shape(Curve.EASE), duration = 1000.0f, roundness = 1.6f, shadow = 0.15f, fadeStart = 0.15f, fadeEnd = 0.55f),
            CloseSettings(x = Spring(200.0f, 1.0f)),
            blur = 5.0f
        ),
        Preset(
            "slowmo",
            "Slow Motion",
            Category.CINEMATIC,
            OpenSettings(main = shape(Curve.SMOOTH, 3.0f), duration = 2500.0f, shadow = 2.0f),
            CloseSettings(x = Spring(60.0f, 1.0f)),
            blur = 2.0f
        ),
        Preset(
            "fog",
            "Fog",
            Category.CINEMATIC,
            OpenSettings(main = shape(Curve.HOLD, 3.0f, aux = 0.35f), duration = 1400.0f, fadeStart = 0.3f, fadeEnd = 0.9f),
            CloseSettings(x = Spring(150.0f, 1.0f)),
            blur = 5.0f
        ),
        Preset(
            "pop",
            "Pop",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.OVERSHOOT, 6.0f), duration = 240.0f, fadeStart = 0.0f, fadeEnd = 0.04f),
            CloseSettings(x = Spring(1100.0f, 0.4f)),
            blur = 0.0f
        ),
        Preset(
            "whip",
            "Whip",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.OVERSHOOT, 8.0f), duration = 200.0f),
            CloseSettings(x = Spring(2200.0f, 0.3f)),
            blur = 0.0f
        ),
        Preset(
            "bounce",
            "Bounce",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.BOUNCE), duration = 1000.0f, horizontal = shape(Curve.DECELERATE, 3.0f)),
            CloseSettings(x = Spring(380.0f, 0.28f))
        ),
        Preset(
            "anticipate",
            "Anticipate",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.ANTICIPATE, 5.0f), duration = 800.0f),
            CloseSettings(x = Spring(500.0f, 0.55f)),
            blur = 1.5f
        ),
        Preset(
            "robot",
            "Robot",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.STEPS, 8.0f), duration = 520.0f, roundness = 0.3f, shadow = 0.0f),
            CloseSettings(x = Spring(1800.0f, 1.0f), roundness = 0.3f),
            blur = 0.0f
        ),
        Preset(
            "pause",
            "Pause",
            Category.PLAYFUL,
            OpenSettings(main = shape(Curve.HOLD, 2.0f, aux = 0.5f), duration = 700.0f),
            CloseSettings(x = Spring(300.0f, 0.6f))
        ),
        Preset(
            "bloom",
            "Bloom",
            Category.SHAPE,
            OpenSettings(main = spring(380.0f, 0.8f), duration = 750.0f, roundness = 2.0f, fadeStart = 0.3f, fadeEnd = 0.7f, originX = 0.0f, originY = 0.0f),
            CloseSettings(x = Spring(380.0f, 0.8f), roundness = 2.0f),
            blur = 2.5f
        ),
        Preset(
            "pill",
            "Pill",
            Category.SHAPE,
            OpenSettings(main = shape(Curve.EASE), duration = 600.0f, roundness = 4.0f, shadow = 0.3f),
            CloseSettings(x = Spring(400.0f, 1.0f), roundness = 4.0f)
        ),
        Preset(
            "square",
            "Square",
            Category.SHAPE,
            OpenSettings(main = bezier(0.3f, 0.0f, 0.0f, 1.0f), duration = 260.0f, roundness = 0.0f, shadow = 0.0f),
            CloseSettings(x = Spring(1400.0f, 0.9f), roundness = 0.0f),
            blur = 0.0f
        ),
        Preset(
            "lift",
            "Lift",
            Category.SHAPE,
            OpenSettings(main = spring(420.0f, 0.72f), duration = 560.0f, roundness = 1.1f, shadow = 6.0f),
            CloseSettings(x = Spring(420.0f, 0.7f)),
            blur = 2.5f
        ),
        Preset(
            "arc",
            "Arc",
            Category.SHAPE,
            OpenSettings(main = spring(900.0f, 0.8f), duration = 600.0f, horizontal = spring(120.0f, 0.95f)),
            CloseSettings(x = Spring(150.0f, 0.95f), y = Spring(900.0f, 0.8f), scale = Spring(500.0f, 0.8f), split = true)
        ),
        Preset(
            "morph",
            "Morph",
            Category.SHAPE,
            OpenSettings(main = spring(420.0f, 0.7f), duration = 700.0f, corner = shape(Curve.OVERSHOOT, 5.0f), roundness = 2.5f),
            CloseSettings(x = Spring(420.0f, 0.7f), roundness = 2.5f)
        )
    )

    fun find(id: String): Preset? = all.firstOrNull { it.id == id }
}
