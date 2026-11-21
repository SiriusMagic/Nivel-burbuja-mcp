package com.zirakusa.nivel

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.ToneGenerator

/**
 * The gravity sensor (or the accelerometer on phones without one), while the app is on screen. [onSample] gets the
 * vector in the phone's axes and the display rotation (0–3) at that moment; the smoothing is done in LevelState.
 */
class TiltSensor(context: Context, private val rotation: () -> Int, private val onSample: (Double, Double, Double, Int) -> Unit) : SensorEventListener {
    private val manager = context.getSystemService(SensorManager::class.java)
    private val sensor: Sensor? = manager?.getDefaultSensor(Sensor.TYPE_GRAVITY) ?: manager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    val available get() = sensor != null

    fun start() { sensor?.let { manager?.registerListener(this, it, SensorManager.SENSOR_DELAY_GAME) } }
    fun stop() { manager?.unregisterListener(this) }

    override fun onSensorChanged(event: SensorEvent) {
        val v = event.values
        onSample(v[0].toDouble(), v[1].toDouble(), v[2].toDouble(), rotation())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
}

/** The flashlight, through the camera's torch (needs no permission). */
class Torch(context: Context) {
    private val manager = context.getSystemService(CameraManager::class.java)
    private val id: String? = runCatching {
        manager?.cameraIdList?.firstOrNull { manager.getCameraCharacteristics(it).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true }
    }.getOrNull()
    val available get() = id != null

    /** False when the camera is busy (another app, or our own preview). */
    fun set(on: Boolean): Boolean = try { id?.let { manager?.setTorchMode(it, on) } != null } catch (_: CameraAccessException) { false }
        catch (_: IllegalArgumentException) { false }
}

/** «Sonido de nivelación»: one short beep, at most once a second. */
class Beeper {
    private val tone = runCatching { ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70) }.getOrNull()
    private var last = 0L

    fun beep() {
        val now = System.currentTimeMillis()
        if (now - last < 1_000) return
        last = now
        tone?.startTone(ToneGenerator.TONE_PROP_BEEP, 120)
    }

    fun release() = tone?.release()
}
