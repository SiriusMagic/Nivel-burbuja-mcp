package com.zirakusa.nivel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color

/** Ajustes › Temas: follow the phone, always light, dark (navy) or pure black (for OLED screens). */
enum class ThemeMode { SYSTEM, LIGHT, DARK, BLACK }

/** Ajustes › Color de acento. [ink] is the text and icons drawn on the accent; [deep] the bubble while not level. */
enum class Accent(val color: Color, val ink: Color, val deep: Color) {
    LIMA(Color(0xFFC6F135), Color(0xFF10180A), Color(0xFF5FA82E)),
    AMARILLO(Color(0xFFF5C518), Color(0xFF111111), Color(0xFFB98A0E)),
    NARANJA(Color(0xFFFF8A3D), Color(0xFF111111), Color(0xFFC25A1C)),
    ROSA(Color(0xFFF06292), Color(0xFF111111), Color(0xFFB23A66)),
    AZUL(Color(0xFF4FC3F7), Color(0xFF0A1420), Color(0xFF2479B8)),
    VERDE(Color(0xFF4CE08A), Color(0xFF07160D), Color(0xFF239457)),
}

/** The app's colours for one mode. [lines] are the faint topographic lines behind the level; [tube] fills the vials. */
data class Palette(val background: Color, val card: Color, val chip: Color, val tube: Color, val lines: Color,
    val text: Color, val muted: Color, val light: Boolean)

private val Navy = Palette(Color(0xFF0D1828), Color(0xFF16243A), Color(0xFF223450), Color(0xFF0A1322), Color(0x1A9FB8D8),
    Color(0xFFEFF3F8), Color(0xFF8C9AB0), light = false)
private val Black = Palette(Color(0xFF000000), Color(0xFF141414), Color(0xFF242424), Color(0xFF0A0A0A), Color(0x14FFFFFF),
    Color(0xFFF2F2F2), Color(0xFF8E8E92), light = false)
private val Light = Palette(Color(0xFFEEF2F6), Color(0xFFFFFFFF), Color(0xFFDCE3EC), Color(0xFFDDE5EE), Color(0x1A2A4466),
    Color(0xFF121A26), Color(0xFF66748A), light = true)

/**
 * The global theme. Every screen reads its colours here; they follow Ajustes › Temas and Color de acento (state, so a
 * change repaints at once). Default: navy with lime, like a field tool.
 */
object LevelColors {
    var palette by mutableStateOf(Navy); private set
    var chosen: com.zirakusa.nivel.ui.theme.Accent by mutableStateOf(com.zirakusa.nivel.ui.theme.Accent.LIMA); private set

    val Background get() = palette.background
    val Card get() = palette.card
    val Chip get() = palette.chip
    val Tube get() = palette.tube
    val Lines get() = palette.lines
    val Text get() = palette.text
    val Muted get() = palette.muted
    val Accent: Color get() = chosen.color
    val OnAccent: Color get() = chosen.ink
    val Bubble: Color get() = chosen.deep
    /** Accent for text and thin lines: the darker shade on light screens, where the bright one is hard to read. */
    val AccentText: Color get() = if (palette.light) chosen.deep else chosen.color
    val Danger = Color(0xFFFF6B6B)
    val isLight get() = palette.light

    /** Changes only what differs, so painting with the same theme never asks for another frame. */
    fun use(mode: ThemeMode, systemDark: Boolean, accent: com.zirakusa.nivel.ui.theme.Accent) {
        val next = when (mode) {
            ThemeMode.SYSTEM -> if (systemDark) Navy else Light
            ThemeMode.LIGHT -> Light
            ThemeMode.DARK -> Navy
            ThemeMode.BLACK -> Black
        }
        if (palette != next) palette = next
        if (chosen != accent) chosen = accent
    }
}

@Composable fun LevelTheme(mode: ThemeMode = ThemeMode.DARK, accent: Accent = Accent.LIMA, content: @Composable () -> Unit) {
    LevelColors.use(mode, isSystemInDarkTheme(), accent)
    val c = LevelColors
    val scheme = if (!c.isLight) darkColorScheme(primary = c.Accent, onPrimary = c.OnAccent, background = c.Background, surface = c.Card,
        surfaceContainer = c.Card, surfaceContainerHigh = c.Card, onBackground = c.Text, onSurface = c.Text, onSurfaceVariant = c.Muted, error = c.Danger)
    else lightColorScheme(primary = c.Accent, onPrimary = c.OnAccent, background = c.Background, surface = c.Card,
        surfaceContainer = c.Card, surfaceContainerHigh = c.Card, onBackground = c.Text, onSurface = c.Text, onSurfaceVariant = c.Muted, error = c.Danger)
    MaterialTheme(scheme) { CompositionLocalProvider(LocalContentColor provides c.Text, content = content) }
}
