package com.zirakusa.nivel

import com.zirakusa.nivel.data.MapValues
import com.zirakusa.nivel.data.Mode
import com.zirakusa.nivel.data.Settings
import com.zirakusa.nivel.data.Tilt
import com.zirakusa.nivel.mcp.LiveReading
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.Tab
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The screen state: calibration, the level beep, saving on each screen (org.json needs Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class LevelStateTest {
    private val values = MapValues()
    private var beeps = 0
    private val state = LevelState(values, now = { 5_000 }, smoothing = 1.0) { beeps++ }

    private fun feed(x: Double, y: Double) = gravityFor(x, y).let { state.onGravity(it[0], it[1], it[2], at = 5_000) }

    @Test fun calibrateMakesTheSurfaceZero() {
        feed(1.2, -0.8)
        assertFalse(state.level)
        state.calibrate()
        assertEquals(0.0, state.tilt.x, 1e-9); assertTrue(state.level)
        assertEquals(1.2, Settings(values).calibration.x, 1e-6)       // remembered
        assertEquals(1.2, LiveReading.latest!!.raw.x, 1e-6)           // the MCP tools read the raw reading
        state.resetCalibration()
        assertEquals(Tilt.ZERO, Settings(values).calibration)
    }

    @Test fun beepsOnceWhenItBecomesLevel() {
        feed(3.0, 0.0); feed(0.2, 0.1)
        assertEquals(0, beeps)                                        // sound off by default
        state.changeSound(true)
        feed(3.0, 0.0); feed(0.2, 0.1); feed(0.1, 0.1)
        assertEquals(1, beeps)
    }

    @Test fun savesWhatTheOpenScreenMeasures() {
        assertNull(state.save())                                      // nothing measured yet
        feed(2.8, 1.5)
        assertEquals(Mode.LEVEL, state.save()!!.mode)
        state.tab = Tab.PLUMB
        assertEquals(Mode.PLUMB, state.save()!!.mode)
        state.tab = Tab.INCLINE
        val incline = state.save()!!
        assertEquals(Mode.INCLINE, incline.mode)
        assertEquals(listOf(3L, 2L, 1L), state.measurements.map { it.id })
        state.delete(incline.id)
        assertEquals(2, state.measurements.size)
    }
}
