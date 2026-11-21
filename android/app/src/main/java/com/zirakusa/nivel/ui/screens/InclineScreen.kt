package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.ui.AppActions
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.theme.LevelColors

/** «Inclinómetro»: the surface's angle (pitch) in a circle at the top, a target in the centre and the roll at the bottom. */
@Composable fun InclineScreen(state: LevelState, actions: AppActions) {
    val incline = state.incline
    Column(Modifier.fillMaxSize().topo().statusBarsPadding(), horizontalAlignment = Alignment.CenterHorizontally) {
        TopRow(stringResource(R.string.incline_title)) { MeasureButtons(state, actions) }
        Column(Modifier.padding(top = 8.dp).size(190.dp).clip(CircleShape).background(LevelColors.Card),
            horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Arrow(Icons.Filled.KeyboardArrowUp, incline.y > 0.05)
            Text(Format.angle(incline.y), fontSize = 40.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
            Text(stringResource(R.string.pitch), color = LevelColors.Muted, fontSize = 13.sp)
            Arrow(Icons.Filled.KeyboardArrowDown, incline.y < -0.05)
        }
        Text(stringResource(R.string.incline_hint), Modifier.padding(start = 24.dp, end = 24.dp, top = 10.dp), color = LevelColors.Muted,
            fontSize = 14.sp, textAlign = TextAlign.Center)
        Box(Modifier.weight(1f).fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
            Canvas(Modifier.fillMaxWidth(0.8f).aspectRatio(1f)) {
                val r = size.minDimension / 2
                val muted = LevelColors.Muted.copy(alpha = 0.5f)
                listOf(1f, 0.66f, 0.33f).forEach { drawCircle(muted, r * it, style = Stroke(1.5.dp.toPx())) }
                drawLine(muted, Offset(center.x, 0f), Offset(center.x, size.height), 1.dp.toPx())
                // The surface's edge, turned by the roll.
                rotate(-incline.x.toFloat()) { drawLine(LevelColors.Accent, Offset(0f, center.y), Offset(size.width, center.y), 3.dp.toPx()) }
                drawLine(muted, Offset(0f, center.y), Offset(size.width, center.y), 1.dp.toPx())
                drawCircle(LevelColors.Accent, 8.dp.toPx())
            }
        }
        Row(Modifier.clip(CircleShape).background(LevelColors.Card).padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically) {
            Arrow(Icons.AutoMirrored.Filled.KeyboardArrowLeft, incline.x < -0.05)
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(Format.angle(incline.x), fontSize = 26.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
                Text(stringResource(R.string.roll), color = LevelColors.Muted, fontSize = 12.sp)
            }
            Arrow(Icons.AutoMirrored.Filled.KeyboardArrowRight, incline.x > 0.05)
        }
        Text(stringResource(R.string.reference), Modifier.padding(top = 14.dp), color = LevelColors.Muted, fontSize = 13.sp)
        Segmented(listOf(stringResource(R.string.ref_front), stringResource(R.string.ref_back)), if (state.front) 0 else 1,
            Modifier.padding(start = 40.dp, end = 40.dp, top = 6.dp, bottom = 16.dp).fillMaxWidth()) { state.front = it == 0 }
    }
}

@Composable private fun Arrow(icon: ImageVector, on: Boolean) =
    Icon(icon, null, Modifier.size(30.dp), tint = if (on) LevelColors.Accent else LevelColors.Muted.copy(alpha = 0.5f))
