package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.data.Tilt
import com.zirakusa.nivel.ui.AppActions
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.theme.LevelColors
import kotlin.math.abs

/** «Plomada»: a weight hanging from a pin near the top, always towards true down; the deviation big at the top. */
@Composable fun PlumbScreen(state: LevelState, actions: AppActions) {
    val angle = state.plumb
    val straight = state.gravity != null && abs(angle) <= Tilt.TOLERANCE
    Column(Modifier.fillMaxSize().topo().statusBarsPadding()) {
        TopRow(stringResource(R.string.plumb_title)) { MeasureButtons(state, actions) }
        Text(Format.angle(angle), Modifier.align(Alignment.CenterHorizontally), fontSize = 56.sp, fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold, color = if (straight) LevelColors.Accent else LevelColors.Text)
        Text(stringResource(R.string.plumb_hint), Modifier.align(Alignment.CenterHorizontally).padding(horizontal = 24.dp),
            color = LevelColors.Muted, fontSize = 14.sp)
        Canvas(Modifier.weight(1f).fillMaxWidth().padding(top = 8.dp)) {
            val pivot = Offset(size.width / 2, 28.dp.toPx())
            val dots = PathEffect.dashPathEffect(floatArrayOf(2.dp.toPx(), 7.dp.toPx()))
            val step = 44.dp.toPx()
            var r = step
            while (r < size.height * 1.1f) {
                drawArc(LevelColors.Muted.copy(alpha = 0.45f), 20f, 140f, false, Offset(pivot.x - r, pivot.y - r), Size(r * 2, r * 2),
                    style = Stroke(1.5.dp.toPx(), cap = StrokeCap.Round, pathEffect = dots))
                r += step
            }
            val length = size.height - pivot.y - 70.dp.toPx()
            // The phone's own vertical axis, to compare against.
            drawLine(LevelColors.Muted.copy(alpha = 0.6f), pivot, Offset(pivot.x, pivot.y + length + 40.dp.toPx()), 1.dp.toPx(),
                pathEffect = PathEffect.dashPathEffect(floatArrayOf(8.dp.toPx(), 6.dp.toPx())))
            rotate(angle.toFloat(), pivot) {
                drawLine(LevelColors.Text, pivot, Offset(pivot.x, pivot.y + length), 1.5.dp.toPx())
                val top = pivot.y + length
                val w = 22.dp.toPx()
                val bob = Path().apply {
                    moveTo(pivot.x - w * 0.35f, top); lineTo(pivot.x + w * 0.35f, top)
                    lineTo(pivot.x + w, top + w * 1.0f); lineTo(pivot.x, top + w * 2.6f); lineTo(pivot.x - w, top + w * 1.0f); close()
                }
                val base = if (straight) LevelColors.Accent else LevelColors.Bubble
                drawPath(bob, Brush.linearGradient(listOf(Color.White.copy(alpha = 0.7f), base, base.copy(red = base.red * 0.5f,
                    green = base.green * 0.5f, blue = base.blue * 0.5f)), Offset(pivot.x - w, top), Offset(pivot.x + w, top + w * 2)))
            }
            drawCircle(LevelColors.Text, 6.dp.toPx(), pivot)
            drawCircle(LevelColors.Background, 2.5.dp.toPx(), pivot)
        }
    }
}
