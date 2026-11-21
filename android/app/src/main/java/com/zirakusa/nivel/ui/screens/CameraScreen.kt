package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
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
import com.zirakusa.nivel.ui.Tab
import com.zirakusa.nivel.ui.theme.LevelColors
import kotlin.math.abs

/**
 * «Cámara»: the camera's picture with the screen's white cross and the true horizon in the accent, turned by the
 * phone's roll, and the angle. Asks for the camera politely first; [preview] is the real camera (a placeholder in tests).
 */
@Composable fun CameraScreen(state: LevelState, actions: AppActions, preview: @Composable (Modifier) -> Unit) {
    val close = { state.tab = Tab.LEVEL }
    Box(Modifier.fillMaxSize().background(Color.Black)) {
        if (!state.cameraAllowed) Permission(actions, close) else {
            preview(Modifier.fillMaxSize())
            val roll = state.roll
            val straight = abs(roll) < 0.5
            Canvas(Modifier.fillMaxSize()) {
                val white = Color.White.copy(alpha = 0.9f)
                drawLine(white, Offset(0f, center.y), Offset(size.width, center.y), 1.5.dp.toPx())
                drawLine(white, Offset(center.x, center.y - 60.dp.toPx()), Offset(center.x, center.y + 60.dp.toPx()), 1.5.dp.toPx())
                rotate(roll.toFloat()) {
                    val reach = size.maxDimension
                    drawLine(LevelColors.Accent, Offset(center.x - reach, center.y), Offset(center.x + reach, center.y), 3.dp.toPx())
                    listOf(-1, 1).forEach { side ->
                        val y = center.y + side * 90.dp.toPx()
                        drawLine(LevelColors.Accent.copy(alpha = 0.7f), Offset(center.x - 110.dp.toPx(), y), Offset(center.x + 110.dp.toPx(), y), 2.dp.toPx())
                    }
                }
                drawCircle(if (straight) LevelColors.Accent else white, 6.dp.toPx())
            }
            Text(Format.wholeDegrees(roll), Modifier.align(Alignment.TopCenter).systemBarsPadding().padding(top = 72.dp)
                .clip(RoundedCornerShape(16.dp)).background(Color.Black.copy(alpha = 0.45f)).padding(horizontal = 18.dp, vertical = 6.dp),
                color = if (straight) LevelColors.Accent else Color.White, fontSize = 44.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold)
        }
        Box(Modifier.align(Alignment.TopEnd).systemBarsPadding().padding(16.dp)) { RoundButton(Icons.Outlined.Close, stringResource(R.string.close), onClick = close) }
    }
}

@Composable private fun Permission(actions: AppActions, close: () -> Unit) =
    Column(Modifier.fillMaxSize().topo().systemBarsPadding().padding(28.dp), verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Outlined.PhotoCamera, null, tint = LevelColors.Accent)
        Text(stringResource(R.string.camera_title), fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text(stringResource(R.string.camera_permission), color = LevelColors.Muted, textAlign = TextAlign.Center, fontSize = 16.sp)
        Button(actions.askCamera, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(LevelColors.Accent, LevelColors.OnAccent)) {
            Text(stringResource(R.string.camera_allow), fontSize = 16.sp)
        }
        TextButton(close) { Text(stringResource(R.string.camera_not_now), color = LevelColors.Text) }
        Text(stringResource(R.string.camera_settings), color = LevelColors.Muted, textAlign = TextAlign.Center, fontSize = 13.sp)
    }
