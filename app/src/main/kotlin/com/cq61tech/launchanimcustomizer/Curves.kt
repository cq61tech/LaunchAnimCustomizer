package com.cq61tech.launchanimcustomizer

import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import kotlin.math.sqrt

class CurveFunction(private val settings: OpenSettings) {
    private val speed = settings.speed.coerceIn(0.2f, 4f)
    private val omega = sqrt(settings.spring.stiffness.coerceAtLeast(1f).toDouble())
    private val zeta = settings.spring.damping.coerceAtLeast(0f).toDouble()
    private val tension = settings.tension.coerceAtLeast(0.1f)
    private val x1 = settings.x1.coerceIn(0f, 1f)
    private val x2 = settings.x2.coerceIn(0f, 1f)
    private val y1 = settings.y1
    private val y2 = settings.y2
    private val blend = settings.curve == Curve.SPRING || speed < 1f

    fun at(input: Float): Float {
        if (input <= 0f) return 0f
        if (input >= 1f) return 1f
        val raw = if (settings.curve == Curve.SPRING) {
            spring(input * HORIZON * speed).toFloat()
        } else {
            shaped((input * speed).coerceIn(0f, 1f))
        }
        if (!blend) return raw
        val weight = ((input - 0.88f) / 0.12f).coerceIn(0f, 1f)
        val eased = weight * weight * (3f - 2f * weight)
        return raw + (1f - raw) * eased
    }

    private fun shaped(u: Float): Float = when (settings.curve) {
        Curve.OVERSHOOT -> overshoot(u)
        Curve.ANTICIPATE -> anticipate(u)
        Curve.BOUNCE -> bounce(u)
        Curve.DECELERATE -> 1f - Math.pow((1f - u).toDouble(), tension.toDouble()).toFloat()
        Curve.EASE -> (cos((u + 1f) * Math.PI) / 2.0 + 0.5).toFloat()
        Curve.BEZIER -> bezier(u)
        Curve.LINEAR, Curve.SPRING -> u
    }

    private fun overshoot(u: Float): Float {
        val t = u - 1f
        return t * t * ((tension + 1f) * t + tension) + 1f
    }

    private fun anticipate(u: Float): Float {
        val s = tension * 1.5f
        return if (u < 0.5f) {
            0.5f * windUp(u * 2f, s)
        } else {
            0.5f * (windDown(u * 2f - 2f, s) + 2f)
        }
    }

    private fun windUp(t: Float, s: Float) = t * t * ((s + 1f) * t - s)

    private fun windDown(t: Float, s: Float) = t * t * ((s + 1f) * t + s)

    private fun bounce(input: Float): Float {
        val t = input * 1.1226f
        return when {
            t < 0.3535f -> fall(t)
            t < 0.7408f -> fall(t - 0.54719f) + 0.7f
            t < 0.9644f -> fall(t - 0.8526f) + 0.9f
            else -> fall(t - 1.0435f) + 0.95f
        }
    }

    private fun fall(t: Float) = t * t * 8f

    private fun bezier(u: Float): Float {
        var low = 0f
        var high = 1f
        var p = u
        repeat(18) {
            p = (low + high) / 2f
            val x = cubic(p, x1, x2)
            if (x < u) low = p else high = p
        }
        return cubic(p, y1, y2)
    }

    private fun cubic(p: Float, a: Float, b: Float): Float {
        val q = 1f - p
        return 3f * q * q * p * a + 3f * q * p * p * b + p * p * p
    }

    private fun spring(time: Double): Double {
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
