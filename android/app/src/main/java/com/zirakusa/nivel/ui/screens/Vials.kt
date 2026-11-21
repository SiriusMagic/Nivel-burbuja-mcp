package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import com.zirakusa.nivel.data.Angles
import com.zirakusa.nivel.data.Tilt
import com.zirakusa.nivel.ui.theme.LevelColors
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.math.min

/**
 * A glass vial with its bubble. Horizontal: [angle] is the X tilt and the bubble goes right when it is positive (the
 * high side). Vertical: [angle] is the Y tilt and the bubble goes up when it is positive.
 */
@Composable fun Vial(angle: Double, vertical: Boolean, modifier: Modifier = Modifier) = Canvas(modifier) {
    val long = if (vertical) size.height else size.width
    val short = if (vertical) size.width else size.height
    val radius = CornerRadius(short / 2, short / 2)
    drawRoundRect(Brush.linearGradient(listOf(LevelColors.Tube, LevelColors.Card, LevelColors.Tube),
        Offset.Zero, if (vertical) Offset(size.width, 0f) else Offset(0f, size.height)), cornerRadius = radius)
    drawRoundRect(LevelColors.Muted.copy(alpha = 0.45f), cornerRadius = radius, style = Stroke(1.5f * density))
    val centred = abs(angle) <= Tilt.TOLERANCE
    val bubble = short * 0.36f
    val bubbleLong = short * 0.62f
    val travel = long / 2 - bubbleLong - short * 0.2f
    val gap = bubbleLong + 3 * density
    for (i in -6..6) {
        if (i == 0) continue
        val at = long / 2 + i * travel / 6
        val mark = if (abs(i) == 6) short * 0.3f else short * 0.16f
        if (abs(at - long / 2) < gap) continue
        line(vertical, at, short / 2 - mark, short / 2 + mark, LevelColors.Muted.copy(alpha = 0.6f), 1.2f * density)
    }
    listOf(-gap, gap).forEach { line(vertical, long / 2 + it, short * 0.12f, short * 0.88f, mark(centred), 2.5f * density) }
    val shift = (Angles.bubble(angle) * travel).toFloat()
    val centre = if (vertical) Offset(size.width / 2, size.height / 2 - shift) else Offset(size.width / 2 + shift, size.height / 2)
    bubble(centre, if (vertical) Size(bubble * 2, bubbleLong * 2) else Size(bubbleLong * 2, bubble * 2), centred)
}

/** The big round level: rings every 5°, cross lines and the bubble, which turns the accent within ±0,5°. */
@Composable fun Bullseye(tilt: Tilt, modifier: Modifier = Modifier) = Canvas(modifier) {
    val r = min(size.width, size.height) / 2 - 4 * density
    val c = center
    drawCircle(Brush.radialGradient(listOf(LevelColors.Card, LevelColors.Tube), c, r), r, c)
    val ring = LevelColors.Muted.copy(alpha = 0.5f)
    drawCircle(ring, r, c, style = Stroke(2f * density))
    val bubble = r * 0.17f
    val travel = r - bubble - 4 * density
    listOf(5.0, 10.0).forEach { drawCircle(ring, (travel * it / Angles.MAX_SCALE).toFloat(), c, style = Stroke(1.2f * density)) }
    drawLine(ring, Offset(c.x - r, c.y), Offset(c.x + r, c.y), 1.2f * density)
    drawLine(ring, Offset(c.x, c.y - r), Offset(c.x, c.y + r), 1.2f * density)
    val level = tilt.isLevel()
    drawCircle(mark(level), bubble + 5 * density, c, style = Stroke(2.5f * density))
    var dx = Angles.bubble(tilt.x) * travel
    var dy = -Angles.bubble(tilt.y) * travel
    val d = hypot(dx, dy)
    if (d > travel) { dx *= travel / d; dy *= travel / d }
    bubble(Offset(c.x + dx.toFloat(), c.y + dy.toFloat()), Size(bubble * 2, bubble * 2), level)
}

private fun mark(level: Boolean) = if (level) LevelColors.Accent else LevelColors.Text.copy(alpha = 0.7f)

private fun DrawScope.line(vertical: Boolean, at: Float, from: Float, to: Float, color: Color, width: Float) =
    if (vertical) drawLine(color, Offset(from, size.height - at), Offset(to, size.height - at), width)
    else drawLine(color, Offset(at, from), Offset(at, to), width)

/** A glossy bubble: light top-left, the colour, a darker rim and a small white shine. */
private fun DrawScope.bubble(centre: Offset, size: Size, level: Boolean) {
    val base = if (level) LevelColors.Accent else LevelColors.Bubble
    val topLeft = Offset(centre.x - size.width / 2, centre.y - size.height / 2)
    val radius = CornerRadius(min(size.width, size.height) / 2)
    drawRoundRect(Brush.radialGradient(listOf(Color.White.copy(alpha = 0.85f).compositeOver(base), base, base.darker()),
        Offset(centre.x - size.width * 0.18f, centre.y - size.height * 0.22f), maxOf(size.width, size.height) * 0.62f), topLeft, size, radius)
    drawRoundRect(base.darker().copy(alpha = 0.8f), topLeft, size, radius, style = Stroke(1.5f * density))
    val shine = Size(size.width * 0.34f, size.height * 0.2f)
    drawOval(Color.White.copy(alpha = 0.55f), Offset(centre.x - size.width * 0.3f, centre.y - size.height * 0.36f), shine)
}

private fun Color.darker() = Color(red * 0.55f, green * 0.55f, blue * 0.55f, alpha)
private fun Color.compositeOver(under: Color) = Color(red * 0.5f + under.red * 0.5f, green * 0.5f + under.green * 0.5f,
    blue * 0.5f + under.blue * 0.5f, 1f)
