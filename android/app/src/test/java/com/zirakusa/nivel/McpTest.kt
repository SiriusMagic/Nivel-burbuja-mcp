package com.zirakusa.nivel

import com.zirakusa.nivel.data.Measurement
import com.zirakusa.nivel.data.Mode
import com.zirakusa.nivel.data.Reading
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.data.Tilt
import com.zirakusa.nivel.mcp.Access
import com.zirakusa.nivel.mcp.HubLink
import com.zirakusa.nivel.mcp.Kind
import com.zirakusa.nivel.mcp.LevelData
import com.zirakusa.nivel.mcp.McpHandler
import com.zirakusa.nivel.mcp.Token
import kotlinx.coroutines.runBlocking
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/** Nivel's MCP: tools list, good calls, bad arguments, unknown tool and no reading, wrong and paused tokens, hub link. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class McpTest {
    private val data = object : LevelData {
        var reading: Reading? = Reading(Tilt(3.3, 1.0), 9_000)
        var calibration = Tilt(0.5, -0.5)
        var saved = listOf(Measurement(2, Mode.PLUMB, -0.9, 0.0, 1_763_600_000_000), Measurement(1, Mode.LEVEL, 2.8, 1.5, 1_763_500_000_000))
        override fun reading() = reading
        override fun calibration() = calibration
        override fun unit() = SlopeUnit.DEGREES
        override fun measurements() = saved
        override fun now() = 10_000L
    }
    private val reader = Token("r", "Claude", "niv_" + "r".repeat(40), Access.READ, Kind.HUB, "Obra")
    private val paused = Token("p", "Viejo", "niv_" + "p".repeat(40), Access.READ, paused = true)
    private val handler = McpHandler({ listOf(reader, paused) }, data, "0.1.0")

    private fun rpc(token: Token, method: String, params: JSONObject = JSONObject()) = runBlocking {
        JSONObject(handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer ${token.secret}"), emptyMap(),
            JSONObject().put("jsonrpc", "2.0").put("id", 1).put("method", method).put("params", params).toString()).body).getJSONObject("result")
    }
    private fun tool(name: String, args: JSONObject) = rpc(reader, "tools/call", JSONObject().put("name", name).put("arguments", args))
    private fun ok(name: String, args: JSONObject = JSONObject()) =
        tool(name, args).also { assertFalse(it.toString(), it.optBoolean("isError")) }.getJSONObject("structuredContent")
    private fun code(name: String, args: JSONObject = JSONObject()) = tool(name, args).also { assertTrue(it.optBoolean("isError")) }
        .getJSONObject("structuredContent").getJSONObject("error").getString("code")

    @Test fun listsTheReadOnlyTools() {
        val tools = rpc(reader, "tools/list").getJSONArray("tools")
        assertEquals(listOf("level_reading", "level_calibration", "level_measurements"), (0 until tools.length()).map { tools.getJSONObject(it).getString("name") })
        assertTrue((0 until tools.length()).all { tools.getJSONObject(it).getJSONObject("annotations").getBoolean("readOnlyHint") })
        val init = rpc(reader, "initialize")
        assertEquals("nivel", init.getJSONObject("serverInfo").getString("name"))
        assertEquals("hub", init.getJSONObject("_meta").getJSONObject(McpHandler.TOKEN_META).getString("kind"))
    }

    @Test fun readsTheLevel() {
        val r = ok("level_reading")
        assertEquals(2.8, r.getDouble("x_degrees"), 1e-9); assertEquals(1.5, r.getDouble("y_degrees"), 1e-9)   // after calibration
        assertFalse(r.getBoolean("is_level")); assertTrue(r.getBoolean("calibrated")); assertEquals(1_000, r.getLong("age_ms"))
        assertEquals("X: 02,8°  Y: 01,5°", r.getJSONObject("slope").getString("text"))
        val percent = ok("level_reading", JSONObject().put("unit", "percent")).getJSONObject("slope")
        assertEquals("percent", percent.getString("unit")); assertEquals(4.9, percent.getDouble("x"), 1e-9)
        data.reading = Reading(Tilt(0.7, -0.3), 9_900)
        assertTrue(ok("level_reading").getBoolean("is_level"))
        val c = ok("level_calibration")
        assertTrue(c.getBoolean("calibrated")); assertEquals(-0.5, c.getDouble("offset_y_degrees"), 1e-9)
        val m = ok("level_measurements", JSONObject().put("limit", 1))
        assertEquals(2, m.getInt("total")); assertEquals(1, m.getJSONArray("measurements").length())
        assertEquals("plumb", m.getJSONArray("measurements").getJSONObject(0).getString("mode"))
        assertEquals("2025-11-20T00:53:20Z", m.getJSONArray("measurements").getJSONObject(0).getString("at"))
    }

    @Test fun badArgumentsAreRefused() {
        assertEquals("BAD_ARGUMENT", code("level_reading", JSONObject().put("sql", "DROP TABLE")))
        assertEquals("BAD_ARGUMENT", code("level_reading", JSONObject().put("unit", "radians")))
        assertEquals("BAD_ARGUMENT", code("level_reading", JSONObject().put("unit", 3)))
        assertEquals("BAD_ARGUMENT", code("level_calibration", JSONObject().put("x", 1)))
        assertEquals("BAD_ARGUMENT", code("level_measurements", JSONObject().put("limit", 0)))
        assertEquals("BAD_ARGUMENT", code("level_measurements", JSONObject().put("limit", "10")))
    }

    @Test fun unknownToolAndNoReading() {
        assertEquals("UNKNOWN_TOOL", code("level_set", JSONObject()))
        data.reading = Reading(Tilt(1.0, 1.0), 1_000)                 // 9 s old: Nivel was closed
        assertEquals("NO_READING", code("level_reading"))
        data.reading = null
        assertEquals("NO_READING", code("level_reading"))
        data.saved = emptyList(); data.calibration = Tilt.ZERO
        assertEquals(0, ok("level_measurements").getInt("total"))
        assertFalse(ok("level_calibration").getBoolean("calibrated"))
    }

    @Test fun wrongAndPausedTokensAndHubLink() = runBlocking {
        val body = """{"jsonrpc":"2.0","id":1,"method":"tools/list"}"""
        assertEquals(401, handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer ${paused.secret}"), emptyMap(), body).status)
        assertEquals(401, handler.handle("POST", "/mcp", mapOf("Authorization" to "Bearer niv_wrong"), emptyMap(), body).status)
        assertEquals(401, handler.handle("POST", "/mcp", emptyMap(), emptyMap(), body).status)
        assertEquals(200, handler.handle("POST", "/mcp", emptyMap(), mapOf("token" to reader.secret), body).status)
        assertEquals(404, handler.handle("POST", "/other", emptyMap(), emptyMap(), body).status)
        assertTrue(Token.secret().startsWith("niv_"))
        assertEquals("NOT_SAME_DEVELOPER", HubLink.answer(false).getString("error"))
        assertEquals("REDIRECT_OFF", HubLink.answer(true).getString("error"))
    }
}
