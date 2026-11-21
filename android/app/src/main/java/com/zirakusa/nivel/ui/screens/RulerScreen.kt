package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.Ruler
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.theme.LevelColors

/**
 * «Regla»: a real-size scale on both long edges of the screen (left and right standing, top and bottom lying), from the
 * screen's density in each direction (xdpi, ydpi). A mark every mm (or 1/16″), the number every cm (or inch).
 */
@Composable fun RulerScreen(state: LevelState) {
    val metrics = LocalResources.current.displayMetrics
    val measurer = rememberTextMeasurer()
    val style = TextStyle(color = LevelColors.Text, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
    Box(Modifier.fillMaxSize().topo()) {
        Canvas(Modifier.fillMaxSize()) {
            val lying = size.width > size.height
            val dpi = if (lying) metrics.xdpi else metrics.ydpi
            val length = (if (lying) size.width else size.height).toDouble()
            val ticks = if (state.inches) Ruler.inchTicks(length, dpi) else Ruler.cmTicks(length, dpi)
            val across = if (lying) size.height else size.width
            val longest = 34.dp.toPx()
            ticks.forEach { t ->
                val at = t.px.toFloat()
                val l = (longest * t.size).toFloat()
                val color = if (t.label != null) LevelColors.Accent else LevelColors.Text.copy(alpha = 0.75f)
                val width = if (t.label != null) 2.dp.toPx() else 1.dp.toPx()
                if (lying) {
                    drawLine(color, Offset(at, 0f), Offset(at, l), width)
                    drawLine(color, Offset(at, across), Offset(at, across - l), width)
                } else {
                    drawLine(color, Offset(0f, at), Offset(l, at), width)
                    drawLine(color, Offset(across, at), Offset(across - l, at), width)
                }
                val label = t.label ?: return@forEach
                if (label == "0") return@forEach
                val text = measurer.measure(label, style)
                val w = text.size.width.toFloat(); val h = text.size.height.toFloat()
                val gap = longest + 4.dp.toPx()
                if (lying) {
                    drawText(text, topLeft = Offset(at - w / 2, gap))
                    drawText(text, topLeft = Offset(at - w / 2, across - gap - h))
                } else {
                    drawText(text, topLeft = Offset(gap, at - h / 2))
                    drawText(text, topLeft = Offset(across - gap - w, at - h / 2))
                }
            }
        }
        Column(Modifier.align(Alignment.Center).width(220.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.tab_ruler), fontSize = 22.sp)
            Segmented(listOf(stringResource(R.string.ruler_cm), stringResource(R.string.ruler_inch)), if (state.inches) 1 else 0,
                Modifier.padding(vertical = 12.dp)) { state.changeInches(it == 1) }
            Text(stringResource(R.string.ruler_hint), color = LevelColors.Muted, fontSize = 14.sp, textAlign = TextAlign.Center)
        }
    }
}
