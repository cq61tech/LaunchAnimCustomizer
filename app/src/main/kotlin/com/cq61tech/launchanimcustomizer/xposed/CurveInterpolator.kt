package com.cq61tech.launchanimcustomizer.xposed

import android.view.animation.Interpolator
import com.cq61tech.launchanimcustomizer.CurveFunction
import com.cq61tech.launchanimcustomizer.CurveSpec

class CurveInterpolator(spec: CurveSpec, durationMs: Float) : Interpolator {
    private val function = CurveFunction(spec, durationMs)

    override fun getInterpolation(input: Float): Float = function.at(input)
}

class FadeInterpolator(start: Float, end: Float) : Interpolator {
    private val from = start.coerceIn(0f, 0.98f)
    private val to = end.coerceIn(from + 0.01f, 1f)

    override fun getInterpolation(input: Float): Float =
        ((input - from) / (to - from)).coerceIn(0f, 1f)
}
