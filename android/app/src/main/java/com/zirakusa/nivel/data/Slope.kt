package com.zirakusa.nivel.data

import java.util.Locale
import kotlin.math.abs
import kotlin.math.round
import kotlin.math.tan

/** Ajustes › Unidad de pendiente. [key] is what the MCP tools say. */
enum class SlopeUnit(val key: String) {
    DEGREES("degrees"), PERCENT("percent"), MM_PER_M("mm_per_m");

    /** The slope of [degrees] in this unit (percent = 100·tan, mm/m = 1000·tan). Near 90° it is capped at [CAP]. */
    fun of(degrees: Double): Double = when (this) {
        DEGREES -> degrees
        PERCENT -> (tan(Math.toRadians(degrees)) * 100).coerceIn(-CAP, CAP)
        MM_PER_M -> (tan(Math.toRadians(degrees)) * 1000).coerceIn(-CAP, CAP)
    }

    companion object {
        const val CAP = 99_999.0
        fun from(key: String?) = entries.firstOrNull { it.key == key || it.name == key }
    }
}

/** Spanish numbers on screen: decimal comma, «02,8°», «4,9 %», «49 mm/m». A rounded −0 is shown as 0. */
object Format {
    private val SPANISH: Locale = Locale.forLanguageTag("es-ES")

    fun round1(value: Double): Double = (round(value * 10) / 10).let { if (it == -0.0) 0.0 else it }

    fun degrees(value: Double): String = signed(round1(value)) { String.format(SPANISH, "%04.1f°", it) }

    fun slope(degrees: Double, unit: SlopeUnit): String {
        val v = unit.of(degrees)
        return when (unit) {
            SlopeUnit.DEGREES -> degrees(degrees)
            SlopeUnit.PERCENT -> signed(round1(v)) { String.format(SPANISH, "%.1f %%", it) }
            SlopeUnit.MM_PER_M -> signed(round(v).let { if (it == -0.0) 0.0 else it }) { String.format(SPANISH, "%.0f mm/m", it) }
        }
    }

    /** Plomada and Inclinómetro: one decimal, no padding, «-0,9°», «-74,3°». */
    fun angle(value: Double): String = signed(round1(value)) { String.format(SPANISH, "%.1f°", it) }

    /** The camera's angle: whole degrees, «-9°». */
    fun wholeDegrees(value: Double): String = "${round(value).toInt().let { if (it == 0) 0 else it }}°"

    private fun signed(value: Double, body: (Double) -> String) = (if (value < 0) "-" else "") + body(abs(value))
}
