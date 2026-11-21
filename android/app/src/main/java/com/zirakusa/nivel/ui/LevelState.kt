package com.zirakusa.nivel.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.zirakusa.nivel.data.Angles
import com.zirakusa.nivel.data.KeyValues
import com.zirakusa.nivel.data.LowPass
import com.zirakusa.nivel.data.Measurement
import com.zirakusa.nivel.data.MeasurementStore
import com.zirakusa.nivel.data.Mode
import com.zirakusa.nivel.data.Pendulum
import com.zirakusa.nivel.data.Plumb
import com.zirakusa.nivel.data.Reading
import com.zirakusa.nivel.data.Settings
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.data.Tilt
import com.zirakusa.nivel.mcp.LiveReading
import com.zirakusa.nivel.ui.theme.Accent
import com.zirakusa.nivel.ui.theme.ThemeMode

/** The bottom bar. */
enum class Tab { LEVEL, PLUMB, INCLINE, RULER, CAMERA, SETTINGS }

/** Pages over the tabs. */
enum class Page { MAIN, MCP, MEASUREMENTS }

/**
 * The only owner of what the screens show: the smoothed gravity (in screen axes) and everything measured from it, the
 * settings and the saved measurements. [smoothing] is the low-pass factor (1 = none, for tests). The sensor feeds [onGravity]; the screens only read and call.
 */
class LevelState(values: KeyValues, private val now: () -> Long = System::currentTimeMillis, smoothing: Double = 0.15,
    private val onLevel: () -> Unit = {}) {
    private val settings = Settings(values)
    private val store = MeasurementStore(values)
    private val filter = LowPass(smoothing)
    private val pendulum = Pendulum()
    private var lastAt = 0L

    var tab by mutableStateOf(Tab.LEVEL)
    var page by mutableStateOf(Page.MAIN)

    /** Gravity in screen axes (x right, y up, z out of the screen), smoothed; null until the sensor answers. */
    var gravity by mutableStateOf<DoubleArray?>(null); private set
    var plumb by mutableDoubleStateOf(0.0); private set
    var calibration by mutableStateOf(settings.calibration); private set
    var unit by mutableStateOf(settings.unit); private set
    var keepScreenOn by mutableStateOf(settings.keepScreenOn); private set
    var sound by mutableStateOf(settings.sound); private set
    var inches by mutableStateOf(settings.inches); private set
    var themeMode by mutableStateOf(settings.theme?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.DARK); private set
    var accent by mutableStateOf(settings.accent?.let { runCatching { Accent.valueOf(it) }.getOrNull() } ?: Accent.LIMA); private set
    /** Inclinómetro › Referencia: true = the screen lies on the surface (cara frontal). */
    var front by mutableStateOf(false)
    var measurements by mutableStateOf(store.list()); private set

    var torch by mutableStateOf(false)
    var cameraAllowed by mutableStateOf(false)

    private var wasLevel = false

    val raw: Tilt get() = gravity?.let { Angles.tilt(it[0], it[1], it[2]) } ?: Tilt.ZERO
    /** The bubble's tilt, after «Calibrar». */
    val tilt: Tilt get() = raw - calibration
    val level get() = gravity != null && tilt.isLevel()
    val roll: Double get() = gravity?.let { Angles.roll(it[0], it[1]) } ?: 0.0
    val incline: Tilt get() = gravity?.let { Plumb.incline(it[0], it[1], it[2], front) } ?: Tilt.ZERO

    /** One sensor sample in the phone's axes; [rotation] is the display's (0–3). */
    fun onGravity(gx: Double, gy: Double, gz: Double, rotation: Int = 0, at: Long = now()) {
        val (sx, sy) = Angles.toScreen(gx, gy, rotation)
        val g = filter.filter(doubleArrayOf(sx, sy, gz))
        gravity = g
        plumb = pendulum.step(Plumb.deviation(g[0], g[1]), if (lastAt == 0L) 0.0 else (at - lastAt) / 1000.0)
        lastAt = at
        LiveReading.latest = Reading(raw, at)
        val isLevel = level
        if (isLevel && !wasLevel && sound) onLevel()
        wasLevel = isLevel
    }

    /** «Calibrar»: what the phone reads now becomes zero. */
    fun calibrate() { calibration = raw; settings.calibration = raw }
    fun resetCalibration() { calibration = Tilt.ZERO; settings.calibration = Tilt.ZERO }

    fun changeUnit(next: SlopeUnit) { unit = next; settings.unit = next }
    fun changeKeepScreenOn(on: Boolean) { keepScreenOn = on; settings.keepScreenOn = on }
    fun changeSound(on: Boolean) { sound = on; settings.sound = on }
    fun changeInches(on: Boolean) { inches = on; settings.inches = on }
    fun changeTheme(mode: ThemeMode) { themeMode = mode; settings.theme = mode.name }
    fun changeAccent(next: Accent) { accent = next; settings.accent = next.name }

    /** «Guardar medición» on the screen that is open. */
    fun save(): Measurement? {
        if (gravity == null) return null
        val (mode, value) = when (tab) {
            Tab.PLUMB -> Mode.PLUMB to Tilt(plumb, 0.0)
            Tab.INCLINE -> Mode.INCLINE to incline.let { Tilt(it.y, it.x) }
            else -> Mode.LEVEL to tilt
        }
        return store.add(mode, value, now()).also { measurements = store.list() }
    }

    fun delete(id: Long) { store.delete(id); measurements = store.list() }
}
