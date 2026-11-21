package com.zirakusa.nivel

import com.zirakusa.nivel.data.Angles
import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.data.LowPass
import com.zirakusa.nivel.data.Pendulum
import com.zirakusa.nivel.data.Plumb
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.data.Tilt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sin

/** Angles from gravity, calibration, slope units, Spanish numbers, plumb and inclinometer: pure maths. */
class LevelMathTest {
    private val e = 1e-6

    @Test fun anglesFromGravity() {
        assertEquals(Tilt.ZERO, Angles.tilt(0.0, 0.0, 9.81))
        val g = gravityFor(2.8, 1.5)
        val t = Angles.tilt(g[0], g[1], g[2])
        assertEquals(2.8, t.x, e); assertEquals(1.5, t.y, e)
        assertTrue(Angles.tilt(-1.0, 0.0, 9.7).x < 0)                 // right edge lower: the bubble goes left
        assertEquals(90.0, Angles.tilt(0.0, 9.81, 0.0).y, e)          // upright
        assertEquals(Tilt.ZERO, Angles.tilt(0.0, 0.0, 0.0))           // free fall: no NaN
        assertEquals(0.0, Angles.roll(0.0, 9.81), e)
        assertEquals(-9.0, Angles.roll(-9.81 * sin(Math.toRadians(9.0)), 9.81 * kotlin.math.cos(Math.toRadians(9.0))), e)
    }

    @Test fun displayRotationAndBubble() {
        assertEquals(1.0 to 2.0, Angles.toScreen(1.0, 2.0, 0))
        assertEquals(-2.0 to 1.0, Angles.toScreen(1.0, 2.0, 1))
        assertEquals(-1.0 to -2.0, Angles.toScreen(1.0, 2.0, 2))
        assertEquals(2.0 to -1.0, Angles.toScreen(1.0, 2.0, 3))
        assertEquals(0.5, Angles.bubble(7.5), e)
        assertEquals(-1.0, Angles.bubble(-40.0), e)
    }

    @Test fun lowPassSmooths() {
        val f = LowPass(0.5)
        assertEquals(0.0, f.filter(doubleArrayOf(0.0))[0], e)
        assertEquals(5.0, f.filter(doubleArrayOf(10.0))[0], e)
        assertEquals(7.5, f.filter(doubleArrayOf(10.0))[0], e)
        f.reset()
        assertEquals(3.0, f.filter(doubleArrayOf(3.0))[0], e)
    }

    @Test fun calibrationAndLevel() {
        val raw = Tilt(1.2, -0.7)
        val calibrated = raw - Tilt(1.0, -0.5)
        assertEquals(0.2, calibrated.x, e); assertEquals(-0.2, calibrated.y, e)
        assertTrue(calibrated.isLevel()); assertFalse(raw.isLevel())
        assertTrue(Tilt(0.5, -0.5).isLevel()); assertFalse(Tilt(0.51, 0.0).isLevel())
    }

    @Test fun slopeUnits() {
        assertEquals(100.0, SlopeUnit.PERCENT.of(45.0), 1e-9)
        assertEquals(1000.0, SlopeUnit.MM_PER_M.of(45.0), 1e-9)
        assertEquals(2.8, SlopeUnit.DEGREES.of(2.8), e)
        assertEquals(SlopeUnit.CAP, SlopeUnit.PERCENT.of(90.0), e)
        assertEquals(SlopeUnit.MM_PER_M, SlopeUnit.from("mm_per_m")); assertEquals(null, SlopeUnit.from("radians"))
    }

    @Test fun spanishNumbers() {
        assertEquals("02,8°", Format.degrees(2.8)); assertEquals("-02,8°", Format.degrees(-2.84))
        assertEquals("00,0°", Format.degrees(-0.04)); assertEquals("12,5°", Format.degrees(12.46))
        assertEquals("4,9 %", Format.slope(2.8, SlopeUnit.PERCENT)); assertEquals("-49 mm/m", Format.slope(-2.8, SlopeUnit.MM_PER_M))
        assertEquals("01,5°", Format.slope(1.5, SlopeUnit.DEGREES))
        assertEquals("-9°", Format.wholeDegrees(-9.2)); assertEquals("0°", Format.wholeDegrees(-0.3))
        assertEquals("-0,9°", Format.angle(-0.94)); assertEquals("-74,3°", Format.angle(-74.3)); assertEquals("0,0°", Format.angle(-0.01))
    }

    @Test fun plumbAndInclinometer() {
        assertEquals(0.0, Plumb.deviation(0.0, 9.81), e)
        assertEquals(-0.9, Plumb.deviation(-9.81 * sin(Math.toRadians(0.9)), 9.81 * kotlin.math.cos(Math.toRadians(0.9))), e)
        assertEquals(0.0, Plumb.incline(0.0, 0.0, 9.81, front = false).y, e)             // flat on its back
        assertEquals(0.0, Plumb.incline(0.0, 0.0, -9.81, front = true).y, e)             // flat on its screen
        assertEquals(90.0, Plumb.incline(0.0, 9.81, 0.0, front = false).y, e)            // upright
        val g = gravityFor(-0.9, 30.0)
        val back = Plumb.incline(g[0], g[1], g[2], front = false)
        assertEquals(-0.9, back.x, 1e-3)
        assertEquals(0.9, Plumb.incline(g[0], g[1], -g[2], front = true).x, 1e-3)        // the other face: mirrored roll
        assertEquals(Tilt.ZERO, Plumb.incline(0.0, 0.0, 0.0, front = false))
    }

    @Test fun pendulumSettles() {
        val p = Pendulum()
        assertEquals(0.0, p.step(0.0, 0.0), e)
        repeat(200) { p.step(10.0, 0.016) }
        assertEquals(10.0, p.angle, 0.05)
        val q = Pendulum(); q.step(0.0, 0.0)
        assertTrue(q.step(10.0, 0.016) < 10.0)        // it swings there, it does not jump
    }
}
