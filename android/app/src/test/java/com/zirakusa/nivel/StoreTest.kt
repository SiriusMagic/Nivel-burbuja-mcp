package com.zirakusa.nivel

import com.zirakusa.nivel.data.MapValues
import com.zirakusa.nivel.data.MeasurementStore
import com.zirakusa.nivel.data.Mode
import com.zirakusa.nivel.data.Ruler
import com.zirakusa.nivel.data.Settings
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.data.Tilt
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** The ruler's scale, the settings and the saved measurements (org.json needs Robolectric). */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class StoreTest {
    @Test fun rulerUsesTheRealDensity() {
        assertEquals(160.0, Ruler.mmToPx(25.4, 160f), 1e-9)
        assertEquals(10.0, Ruler.pxToMm(Ruler.mmToPx(10.0, 403f), 403f), 1e-9)
        val cm = Ruler.cmTicks(Ruler.mmToPx(50.0, 320f), 320f)
        assertEquals(51, cm.size)                                     // 0 to 50 mm
        assertEquals(listOf("0", "1", "2", "3", "4", "5"), cm.mapNotNull { it.label })
        assertEquals(Ruler.mmToPx(10.0, 320f), cm[10].px, 1e-9)
        assertEquals(0.7, cm[5].size, 1e-9)
        val inch = Ruler.inchTicks(640.0, 320f)                       // two inches
        assertEquals(33, inch.size); assertEquals(listOf("0", "1", "2"), inch.mapNotNull { it.label })
    }

    @Test fun settingsKeepTheirValues() {
        val values = MapValues()
        val s = Settings(values)
        assertEquals(Tilt.ZERO, s.calibration); assertFalse(s.calibrated); assertEquals(SlopeUnit.DEGREES, s.unit)
        s.calibration = Tilt(1.5, -0.25); s.unit = SlopeUnit.PERCENT; s.sound = true; s.keepScreenOn = true
        val again = Settings(values)
        assertEquals(Tilt(1.5, -0.25), again.calibration); assertTrue(again.calibrated)
        assertEquals(SlopeUnit.PERCENT, again.unit); assertTrue(again.sound); assertTrue(again.keepScreenOn); assertFalse(again.inches)
        values.put("cal_x", "NaN"); values.put("unit", "radians")
        assertEquals(Tilt.ZERO, again.calibration); assertEquals(SlopeUnit.DEGREES, again.unit)
        again.calibration = Tilt.ZERO
        assertEquals(null, values.get("cal_y"))
    }

    @Test fun measurementsAreSavedNewestFirst() {
        val values = MapValues()
        val store = MeasurementStore(values)
        assertTrue(store.list().isEmpty())
        store.add(Mode.LEVEL, Tilt(2.84, 1.46), 1_000)
        val plumb = store.add(Mode.PLUMB, Tilt(-0.9, 0.0), 2_000)
        assertEquals(listOf(Mode.PLUMB, Mode.LEVEL), MeasurementStore(values).list().map { it.mode })
        assertEquals(2.8, store.list().last().x, 1e-9)               // one decimal, like the screen
        store.delete(plumb.id)
        assertEquals(1, store.list().size)
        values.put("measurements", "not json")
        assertTrue(store.list().isEmpty())
        repeat(MeasurementStore.MAX + 3) { store.add(Mode.INCLINE, Tilt(it.toDouble(), 0.0), it.toLong()) }
        assertEquals(MeasurementStore.MAX, store.list().size)
    }
}
