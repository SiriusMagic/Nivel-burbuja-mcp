package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.ui.theme.LevelColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Round button with one icon (flashlight, save, settings, back…). [on] paints it with the accent. */
@Composable fun RoundButton(icon: ImageVector, label: String, modifier: Modifier = Modifier, on: Boolean = false, onClick: () -> Unit) =
    Box(modifier.size(48.dp).clip(CircleShape).background(if (on) LevelColors.Accent else LevelColors.Card).clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center) { Icon(icon, label, tint = if (on) LevelColors.OnAccent else LevelColors.Text) }

/** The row at the top of the measuring screens: buttons on the left, the title in the middle, buttons on the right. */
@Composable fun TopRow(title: String, left: @Composable RowScope.() -> Unit = {}, right: @Composable RowScope.() -> Unit = {}) =
    Box(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).height(48.dp)) {
        Row(Modifier.align(Alignment.CenterStart), horizontalArrangement = Arrangement.spacedBy(8.dp), content = left)
        Text(title, Modifier.align(Alignment.Center), fontSize = 20.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Row(Modifier.align(Alignment.CenterEnd), horizontalArrangement = Arrangement.spacedBy(8.dp), content = right)
    }

/** One choice among a few (units, theme, reference), as a pill. */
@Composable fun Segmented(options: List<String>, selected: Int, modifier: Modifier = Modifier, onSelect: (Int) -> Unit) =
    Row(modifier.clip(CircleShape).background(LevelColors.Chip).padding(4.dp), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        options.forEachIndexed { i, label ->
            Text(label, Modifier.weight(1f).clip(CircleShape).background(if (i == selected) LevelColors.Accent else LevelColors.Chip)
                .clickable { onSelect(i) }.padding(horizontal = 10.dp, vertical = 9.dp),
                color = if (i == selected) LevelColors.OnAccent else LevelColors.Text, fontSize = 14.sp, textAlign = TextAlign.Center,
                maxLines = 1, fontWeight = if (i == selected) FontWeight.SemiBold else FontWeight.Normal)
        }
    }

/** The faint topographic lines behind the measuring screens: wavy closed contours around a few hills (always the same). */
fun Modifier.topo(): Modifier = drawBehind {
    drawRect(LevelColors.Background)
    val hills = listOf(Triple(0.15f, 0.2f, 1.0f), Triple(0.85f, 0.55f, 1.6f), Triple(0.3f, 0.9f, 2.3f))
    val stroke = Stroke(1.dp.toPx())
    hills.forEach { (hx, hy, seed) ->
        val centre = Offset(size.width * hx, size.height * hy)
        for (ring in 1..9) {
            val base = ring * 34.dp.toPx()
            val path = Path()
            for (i in 0..72) {
                val a = i / 72.0 * 2 * PI
                val r = base * (1 + 0.12 * sin(3 * a + seed + ring * 0.4) + 0.07 * cos(5 * a - seed * 2)).toFloat()
                val p = Offset(centre.x + r * cos(a).toFloat(), centre.y + r * sin(a).toFloat())
                if (i == 0) path.moveTo(p.x, p.y) else path.lineTo(p.x, p.y)
            }
            path.close()
            drawPath(path, LevelColors.Lines, style = stroke)
        }
    }
}
