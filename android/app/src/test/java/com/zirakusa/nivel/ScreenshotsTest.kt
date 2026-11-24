package com.zirakusa.nivel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.isRoot
import com.github.takahirom.roborazzi.captureRoboImage
import com.zirakusa.nivel.data.MapValues
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.NivelApp
import com.zirakusa.nivel.ui.Page
import com.zirakusa.nivel.ui.Tab
import com.zirakusa.nivel.ui.theme.Accent
import com.zirakusa.nivel.ui.theme.LevelTheme
import com.zirakusa.nivel.ui.theme.ThemeMode
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import kotlin.math.cos
import kotlin.math.sin

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], qualifiers = "es-w411dp-h891dp-xhdpi")
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class ScreenshotsTest {
    @get:Rule val compose = createComposeRule()
    private val state = LevelState(MapValues(), now = { 1_763_600_000_000 }, smoothing = 1.0)

    private var t = 1L
    /** Feeds the same sample for 2,4 s of sensor time, so the plumb bob's swing settles too. */
    private fun feed(x: Double, y: Double, z: Double) = repeat(150) { t += 16; state.onGravity(x, y, z, at = t) }
    private fun lean(x: Double, y: Double) = gravityFor(x, y).let { feed(it[0], it[1], it[2]) }
    private fun upright(roll: Double) = feed(9.81 * sin(Math.toRadians(roll)), 9.81 * cos(Math.toRadians(roll)), 0.4)
    private fun shot(name: String) { compose.waitForIdle(); onView(isRoot()).captureRoboImage("build/screenshots/nivel-$name.png") }

    /** The camera's picture stands in as a sky-and-ground gradient (Robolectric has no camera). */
    private val placeholder: @Composable (Modifier) -> Unit = { m ->
        Box(m.background(Brush.verticalGradient(listOf(Color(0xFF6E8FB5), Color(0xFFB9C7D6), Color(0xFF5C5445), Color(0xFF3A342B)))))
    }

    @Test fun levelPlumbInclineAndRuler() {
        compose.setContent { LevelTheme(state.themeMode, state.accent) { NivelApp(state, preview = placeholder) } }
        lean(2.8, 1.5)
        compose.onNodeWithText("X: 02,8°").assertExists()
        compose.onNodeWithText("Y: 01,5°").assertExists()
        shot("level")
        lean(0.2, -0.3)
        compose.onNodeWithText("X: 00,2°").assertExists()
        shot("level-centred")
        lean(2.8, 1.5)
        compose.onNodeWithText("Calibrar").performClick()
        compose.onNodeWithText("X: 00,0°").assertExists()
        state.changeUnit(SlopeUnit.PERCENT); lean(3.6, 2.0)
        compose.onNodeWithText("X: 1,4 %").assertExists()

        compose.onNodeWithText("Plomada").performClick()
        upright(-0.9)
        compose.onNodeWithText("-0,9°").assertExists()
        shot("plumb")
        compose.onNodeWithText("Ángulo").performClick()
        feed(-0.15, -9.45, 2.64)
        compose.onNodeWithText("Referencia").assertExists()
        shot("incline")
        compose.onNodeWithText("Cara frontal").performClick()
        assertEquals(true, state.front)

        compose.onNodeWithText("Regla").performClick()
        compose.onNodeWithText("pulg").assertExists()
        shot("ruler")
        compose.onNodeWithText("pulg").performClick()
        shot("ruler-inches")
    }

    @Test fun cameraMeasurementsSettingsAndMcp() {
        compose.setContent { LevelTheme(state.themeMode, state.accent) { NivelApp(state, preview = placeholder) } }
        state.tab = Tab.CAMERA
        compose.onNodeWithText("Permitir cámara").assertExists()
        shot("camera-permission")
        state.cameraAllowed = true; upright(-9.0)
        compose.onNodeWithText("-9°").assertExists()
        shot("camera")
        compose.onNodeWithContentDescription("Cerrar").performClick()

        compose.onNodeWithContentDescription("Mediciones guardadas").performClick()
        compose.onNodeWithText("Aún no hay mediciones").assertExists()
        shot("measurements-empty")
        compose.onNodeWithText("Empezar a medir").performClick()
        lean(2.8, 1.5)
        compose.onNodeWithContentDescription("Guardar medición").performClick()
        state.tab = Tab.PLUMB; upright(-0.9); state.save()
        state.tab = Tab.INCLINE; feed(-0.15, -9.45, 2.64); state.save()
        state.page = Page.MEASUREMENTS
        compose.onNodeWithText("Inclinómetro").assertExists()
        shot("measurements")
        state.page = Page.MAIN

        state.tab = Tab.SETTINGS
        compose.onNodeWithText("Restablecer calibración").assertExists()
        shot("settings")
        compose.onNodeWithText("Modo MCP").performScrollTo().performClick()
        compose.onNodeWithText("Redireccionar a Hub").assertExists()
        shot("mcp")
    }

    @Test fun lightAndBlackThemes() {
        compose.setContent { LevelTheme(state.themeMode, state.accent) { NivelApp(state, preview = placeholder) } }
        state.changeTheme(ThemeMode.LIGHT); state.changeAccent(Accent.AZUL)
        lean(-4.0, 2.5)
        shot("level-light")
        state.tab = Tab.SETTINGS
        compose.onNodeWithText("Negro").performScrollTo().performClick()
        compose.onNodeWithContentDescription("Lima").performScrollTo().performClick()
        assertEquals(ThemeMode.BLACK, state.themeMode); assertEquals(Accent.LIMA, state.accent)
        shot("settings-black")
        state.tab = Tab.LEVEL
        shot("level-black")
    }
}
