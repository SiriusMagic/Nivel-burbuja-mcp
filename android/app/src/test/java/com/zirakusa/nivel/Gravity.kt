package com.zirakusa.nivel

import kotlin.math.sin
import kotlin.math.sqrt

/** The gravity vector (pointing up, 9,81 m/s²) that the phone reads when its axes lean [x] and [y] degrees. */
fun gravityFor(x: Double, y: Double, g: Double = 9.81): DoubleArray {
    val gx = g * sin(Math.toRadians(x)); val gy = g * sin(Math.toRadians(y))
    return doubleArrayOf(gx, gy, sqrt(g * g - gx * gx - gy * gy))
}
