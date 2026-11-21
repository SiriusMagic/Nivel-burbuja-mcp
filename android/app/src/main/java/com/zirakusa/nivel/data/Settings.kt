package com.zirakusa.nivel.data

import androidx.core.content.edit

/** Where the settings live: the phone's preferences in the app, a map in the tests. */
interface KeyValues {
    fun get(key: String): String?
    fun put(key: String, value: String?)
}

class MapValues(private val map: MutableMap<String, String> = mutableMapOf()) : KeyValues {
    override fun get(key: String) = map[key]
    override fun put(key: String, value: String?) { if (value == null) map.remove(key) else map[key] = value }
}

/** Every setting of Nivel, typed. Unknown or broken values fall back to the defaults. */
class Settings(private val kv: KeyValues) {
    /** «Calibrar»: the tilt that counts as zero (what the phone read on the surface the user calls level). */
    var calibration: Tilt
        get() {
            val x = kv.get(CAL_X)?.toDoubleOrNull() ?: return Tilt.ZERO
            val y = kv.get(CAL_Y)?.toDoubleOrNull() ?: return Tilt.ZERO
            return if (x.isFinite() && y.isFinite()) Tilt(x, y) else Tilt.ZERO
        }
        set(value) {
            kv.put(CAL_X, value.x.takeIf { value != Tilt.ZERO }?.toString())
            kv.put(CAL_Y, value.y.takeIf { value != Tilt.ZERO }?.toString())
        }
    val calibrated get() = calibration != Tilt.ZERO

    var unit: SlopeUnit
        get() = SlopeUnit.from(kv.get(UNIT)) ?: SlopeUnit.DEGREES
        set(value) = kv.put(UNIT, value.key)
    var keepScreenOn: Boolean
        get() = kv.get(KEEP_ON) == "1"
        set(value) = kv.put(KEEP_ON, if (value) "1" else null)
    var sound: Boolean
        get() = kv.get(SOUND) == "1"
        set(value) = kv.put(SOUND, if (value) "1" else null)
    var inches: Boolean
        get() = kv.get(INCHES) == "1"
        set(value) = kv.put(INCHES, if (value) "1" else null)
    var theme: String?
        get() = kv.get(THEME)
        set(value) = kv.put(THEME, value)
    var accent: String?
        get() = kv.get(ACCENT)
        set(value) = kv.put(ACCENT, value)

    private companion object {
        const val CAL_X = "cal_x"; const val CAL_Y = "cal_y"; const val UNIT = "unit"; const val KEEP_ON = "keep_on"
        const val SOUND = "sound"; const val INCHES = "inches"; const val THEME = "theme"; const val ACCENT = "accent"
    }
}

/** The latest smoothed reading (raw, before calibration) and when it was taken. */
data class Reading(val raw: Tilt, val at: Long)

/** The phone's preferences file «nivel» (kept out of backups). Shared with the MCP tools read the same. */
class SharedValues(context: android.content.Context) : KeyValues {
    private val prefs = context.applicationContext.getSharedPreferences("nivel", android.content.Context.MODE_PRIVATE)
    override fun get(key: String): String? = prefs.getString(key, null)
    override fun put(key: String, value: String?) {
        prefs.edit { if (value == null) remove(key) else putString(key, value) }
    }
}
