package com.zirakusa.nivel.data

import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.sqrt

/** Angles in degrees: [x] is how far the screen's left-right axis leans, [y] the top-bottom one (0 = horizontal). */
data class Tilt(val x: Double, val y: Double) {
    operator fun minus(other: Tilt) = Tilt(x - other.x, y - other.y)

    /** Level when both axes are within [tolerance] degrees (±0,5° by default). */
    fun isLevel(tolerance: Double = TOLERANCE) = abs(x) <= tolerance && abs(y) <= tolerance

    companion object {
        const val TOLERANCE = 0.5
        val ZERO = Tilt(0.0, 0.0)
    }
}

/** Pure maths from the gravity vector (m/s², the phone's own axes): no Android here, so all of it is tested. */
object Angles {
    private const val DEG = 180.0 / Math.PI

    /**
     * Each screen axis' angle from the horizontal. Gravity read by the sensor points up (+z when the phone lies flat);
     * a negative x means the right edge is lower, so the bubble (the high side) goes left.
     */
    fun tilt(gx: Double, gy: Double, gz: Double): Tilt {
        val g = sqrt(gx * gx + gy * gy + gz * gz)
        if (g < 1e-6) return Tilt.ZERO
        return Tilt(asin((gx / g).coerceIn(-1.0, 1.0)) * DEG, asin((gy / g).coerceIn(-1.0, 1.0)) * DEG)
    }

    /** The phone held upright (camera level): how far it is turned around the screen's normal. 0 = straight, + = clockwise. */
    fun roll(gx: Double, gy: Double): Double = if (abs(gx) < 1e-9 && abs(gy) < 1e-9) 0.0 else atan2(gx, gy) * DEG

    /**
     * The sensor's axes follow the phone, the screen's follow the display rotation (0, 1, 2, 3 = 0°, 90°, 180°, 270°),
     * like Android's «Accelerometer Play» sample. Returns the gravity in screen axes (x right, y up).
     */
    fun toScreen(gx: Double, gy: Double, rotation: Int): Pair<Double, Double> = when (rotation and 3) {
        1 -> -gy to gx
        2 -> -gx to -gy
        3 -> gy to -gx
        else -> gx to gy
    }

    /** Where a bubble sits along its scale, from -1 to 1: [max] degrees (or more) puts it at the end. */
    fun bubble(angle: Double, max: Double = MAX_SCALE): Double = (angle / max).coerceIn(-1.0, 1.0)

    const val MAX_SCALE = 15.0
}

/** Smooths the jittery sensor: each new value moves the output [alpha] of the way (low-pass filter). */
class LowPass(private val alpha: Double = 0.15) {
    private var last: DoubleArray? = null

    fun filter(values: DoubleArray): DoubleArray {
        val previous = last
        val out = if (previous == null || previous.size != values.size) values.copyOf()
        else DoubleArray(values.size) { previous[it] + alpha * (values[it] - previous[it]) }
        last = out
        return out
    }

    fun reset() { last = null }
}
