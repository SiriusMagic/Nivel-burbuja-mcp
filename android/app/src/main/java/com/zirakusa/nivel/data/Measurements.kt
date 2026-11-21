package com.zirakusa.nivel.data

import org.json.JSONArray
import org.json.JSONObject

/** Which screen measured it. [key] is what the MCP tools say. */
enum class Mode(val key: String) {
    LEVEL("level"), PLUMB("plumb"), INCLINE("incline");
    companion object { fun from(key: String?) = entries.firstOrNull { it.key == key } }
}

/**
 * One saved reading («Guardar medición»). LEVEL: [x] and [y] are the calibrated tilt; PLUMB: [x] is the deviation from
 * vertical; INCLINE: [x] is the pitch and [y] the roll. Degrees always; [at] in ms since 1970.
 */
data class Measurement(val id: Long, val mode: Mode, val x: Double, val y: Double, val at: Long) {
    fun json(): JSONObject = JSONObject().put("id", id).put("mode", mode.key).put("x", x).put("y", y).put("at", at)
}

/** «Mediciones guardadas»: kept on the phone (the app's preferences, as JSON), newest first, at most [MAX]. */
class MeasurementStore(private val kv: KeyValues) {
    fun list(): List<Measurement> = runCatching {
        val array = JSONArray(kv.get(KEY) ?: "[]")
        (0 until array.length()).map { array.getJSONObject(it) }.mapNotNull { o ->
            val mode = Mode.from(o.optString("mode")) ?: return@mapNotNull null
            Measurement(o.getLong("id"), mode, o.getDouble("x"), o.getDouble("y"), o.getLong("at"))
        }
    }.getOrDefault(emptyList())

    fun add(mode: Mode, tilt: Tilt, at: Long): Measurement {
        val all = list()
        val m = Measurement((all.maxOfOrNull { it.id } ?: 0) + 1, mode, Format.round1(tilt.x), Format.round1(tilt.y), at)
        save((listOf(m) + all).take(MAX))
        return m
    }

    fun delete(id: Long) = save(list().filterNot { it.id == id })

    private fun save(list: List<Measurement>) = kv.put(KEY, JSONArray(list.map { it.json() }).toString())

    companion object {
        const val MAX = 500
        private const val KEY = "measurements"
    }
}
