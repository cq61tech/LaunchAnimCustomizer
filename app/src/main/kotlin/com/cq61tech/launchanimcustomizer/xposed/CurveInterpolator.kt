package com.cq61tech.launchanimcustomizer.xposed

import android.view.animation.Interpolator
import com.cq61tech.launchanimcustomizer.CurveFunction
import com.cq61tech.launchanimcustomizer.OpenSettings

class CurveInterpolator(settings: OpenSettings) : Interpolator {
    private val function = CurveFunction(settings)

    override fun getInterpolation(input: Float): Float = function.at(input)
}
