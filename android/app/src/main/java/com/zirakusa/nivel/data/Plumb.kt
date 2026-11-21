package com.zirakusa.nivel.data

import kotlin.math.atan2
import kotlin.math.sqrt

/** Plomada and Inclinómetro: more pure maths from the gravity vector (m/s², the phone's axes, pointing up). */
object Plumb {
    private const val DEG = 180.0 / Math.PI

    /**
     * Phone upright against a wall: how far its long (vertical) axis is from true vertical, in degrees. The plumb line
     * hangs at minus this angle on the screen (it always points to true down). Same as the camera's roll.
     */
    fun deviation(gx: Double, gy: Double): Double = Angles.roll(gx, gy)

    /**
     * Inclinómetro: [pitch] is the angle of the face lying on the surface from horizontal (0 flat, 90 upright), along the
     * phone's long axis; [roll] its sideways lean. [front] = the screen is the face against the surface.
     */
    fun incline(gx: Double, gy: Double, gz: Double, front: Boolean): Tilt {
        val z = if (front) -gz else gz
        val x = if (front) -gx else gx
        if (gx * gx + gy * gy + gz * gz < 1e-12) return Tilt.ZERO
        return Tilt(atan2(x, sqrt(gy * gy + z * z)) * DEG, atan2(gy, z) * DEG)
    }
}

/**
 * The plumb bob swings a little before it rests: a damped spring towards the [target] angle. [step] moves it [dt]
 * seconds. With these constants it settles in about a second without wobbling forever.
 */
class Pendulum(private val stiffness: Double = 60.0, private val damping: Double = 11.0) {
    var angle = Double.NaN; private set
    private var velocity = 0.0

    fun step(target: Double, dt: Double): Double {
        if (angle.isNaN()) { angle = target; return angle }
        val t = dt.coerceIn(0.0, 0.1)
        velocity += (stiffness * (target - angle) - damping * velocity) * t
        angle += velocity * t
        return angle
    }
}
