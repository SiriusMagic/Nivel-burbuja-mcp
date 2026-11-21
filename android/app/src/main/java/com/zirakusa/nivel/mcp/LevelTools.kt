package com.zirakusa.nivel.mcp

import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.data.Measurement
import com.zirakusa.nivel.data.Reading
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.data.Tilt
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

/** What the tools read; the app gives the live sensor and its settings, tests a fake. Nothing here changes anything. */
interface LevelData {
    /** The latest smoothed reading, before calibration; null when the phone has not measured. */
    fun reading(): Reading?
    fun calibration(): Tilt
    fun unit(): SlopeUnit
    fun measurements(): List<Measurement>
    fun now(): Long = System.currentTimeMillis()
}

/** An expected failure the AI can read (structuredContent.error.code), never a crash. */
class ToolFailure(val code: String, message: String) : Exception(message)

/**
 * Nivel's MCP tools, all read-only: the live tilt, the calibration and the saved measurements. Arguments are checked
 * one by one; nothing is ever run as code.
 */
object LevelTools {
    class Tool(val name: String, val description: String, val write: Boolean, val schema: JSONObject)

    /** A reading older than this is not live any more (Nivel was closed): the tool answers NO_READING. */
    const val FRESH_MS = 5_000L

    private fun schema(vararg props: Pair<String, JSONObject>) = JSONObject()
        .put("type", "object").put("properties", JSONObject().apply { props.forEach { (k, v) -> put(k, v) } })
        .put("required", JSONArray()).put("additionalProperties", false)

    val tools = listOf(
        Tool("level_reading", "The phone's live tilt: x (left-right) and y (top-bottom) in degrees after calibration, the slope " +
            "in a unit, and is_level (both within ±${Tilt.TOLERANCE}°). Nivel must be open on the phone.", false,
            schema("unit" to JSONObject().put("type", "string").put("enum", JSONArray(SlopeUnit.entries.map { it.key }))
                .put("description", "Slope unit; default the one chosen in Nivel › Ajustes."))),
        Tool("level_calibration", "The zero the user set with «Calibrar»: offsets in degrees (0 and calibrated=false when never set).", false, schema()),
        Tool("level_measurements", "Measurements the user saved (newest first): mode level/plumb/incline, x and y in degrees, date.", false,
            schema("limit" to JSONObject().put("type", "integer").put("description", "How many, 1–100 (default 20)."))),
    )

    fun available(@Suppress("UNUSED_PARAMETER") access: Access) = tools

    fun call(name: String, args: JSONObject, @Suppress("UNUSED_PARAMETER") access: Access, data: LevelData): JSONObject {
        val tool = tools.firstOrNull { it.name == name } ?: throw ToolFailure("UNKNOWN_TOOL", "No tool called «$name».")
        val allowed = tool.schema.getJSONObject("properties").keys().asSequence().toSet()
        args.keys().forEach { if (it !in allowed) throw ToolFailure("BAD_ARGUMENT", "Unknown argument «$it».") }
        return when (name) {
            "level_reading" -> reading(args, data)
            "level_calibration" -> data.calibration().let { c ->
                JSONObject().put("calibrated", c != Tilt.ZERO).put("offset_x_degrees", Format.round1(c.x)).put("offset_y_degrees", Format.round1(c.y))
            }
            else -> measurements(args, data)
        }
    }

    private fun reading(args: JSONObject, data: LevelData): JSONObject {
        val unit = if (!args.has("unit")) data.unit() else (args.opt("unit") as? String)?.let(SlopeUnit::from)
            ?: throw ToolFailure("BAD_ARGUMENT", "unit must be one of ${SlopeUnit.entries.joinToString { it.key }}.")
        val reading = data.reading()?.takeIf { data.now() - it.at <= FRESH_MS }
            ?: throw ToolFailure("NO_READING", "No live reading: open Nivel on the phone (it needs an accelerometer).")
        val calibration = data.calibration()
        val tilt = reading.raw - calibration
        return JSONObject().put("x_degrees", Format.round1(tilt.x)).put("y_degrees", Format.round1(tilt.y))
            .put("slope", JSONObject().put("unit", unit.key).put("x", Format.round1(unit.of(tilt.x))).put("y", Format.round1(unit.of(tilt.y)))
                .put("text", "X: ${Format.slope(tilt.x, unit)}  Y: ${Format.slope(tilt.y, unit)}"))
            .put("is_level", tilt.isLevel()).put("tolerance_degrees", Tilt.TOLERANCE).put("calibrated", calibration != Tilt.ZERO)
            .put("age_ms", data.now() - reading.at)
    }

    private fun measurements(args: JSONObject, data: LevelData): JSONObject {
        val limit = if (!args.has("limit")) 20 else (args.opt("limit") as? Int)?.takeIf { it in 1..100 }
            ?: throw ToolFailure("BAD_ARGUMENT", "limit must be an integer from 1 to 100.")
        val all = data.measurements()
        return JSONObject().put("total", all.size).put("measurements", JSONArray(all.take(limit).map {
            JSONObject().put("id", it.id).put("mode", it.mode.key).put("x_degrees", it.x).put("y_degrees", it.y)
                .put("at", Instant.ofEpochMilli(it.at).toString())
        }))
    }
}
