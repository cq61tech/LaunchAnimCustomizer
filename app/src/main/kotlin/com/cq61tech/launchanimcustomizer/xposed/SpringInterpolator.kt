package com.cq61tech.launchanimcustomizer.xposed

import android.view.animation.Interpolator
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class SpringInterpolator(stiffness: Float, damping: Float) : Interpolator {
    private val omega = sqrt(stiffness.toDouble())
    private val zeta = damping.toDouble()

    override fun getInterpolation(input: Float): Float {
        if (input <= 0f) return 0f
        if (input >= 1f) return 1f
        val raw = response(input * HORIZON).toFloat()
        val blend = ((input - 0.85f) / 0.15f).coerceIn(0f, 1f)
        val eased = blend * blend * (3f - 2f * blend)
        return raw + (1f - raw) * eased
    }

    private fun response(time: Double): Double {
        if (abs(zeta - 1.0) < 1e-4) {
            return 1.0 - exp(-omega * time) * (1.0 + omega * time)
        }
        if (zeta < 1.0) {
            val damped = omega * sqrt(1.0 - zeta * zeta)
            val decay = exp(-zeta * omega * time)
            return 1.0 - decay * (cos(damped * time) + zeta * omega / damped * sin(damped * time))
        }
        val spread = omega * sqrt(zeta * zeta - 1.0)
        val slow = -zeta * omega + spread
        val fast = -zeta * omega - spread
        return 1.0 - (fast * exp(slow * time) - slow * exp(fast * time)) / (fast - slow)
    }

    private companion object {
        const val HORIZON = 0.5
    }
}
