package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.List
import androidx.compose.material.icons.outlined.BookmarkAdd
import androidx.compose.material.icons.outlined.FlashlightOff
import androidx.compose.material.icons.outlined.FlashlightOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.ui.AppActions
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.Page
import com.zirakusa.nivel.ui.Tab
import com.zirakusa.nivel.ui.theme.LevelColors

/** «Nivel»: a horizontal vial (X), a vertical one (Y), the round level, the readouts and «Calibrar». */
@Composable fun LevelScreen(state: LevelState, actions: AppActions) {
    val tilt = state.tilt
    Column(Modifier.fillMaxSize().topo().statusBarsPadding()) {
        TopRow("", left = {
            if (actions.hasFlash) RoundButton(if (state.torch) Icons.Outlined.FlashlightOn else Icons.Outlined.FlashlightOff,
                stringResource(R.string.flashlight), on = state.torch) { actions.torch(!state.torch) }
        }) { MeasureButtons(state, actions); RoundButton(Icons.Outlined.Settings, stringResource(R.string.settings)) { state.tab = Tab.SETTINGS } }
        Vial(tilt.x, vertical = false, modifier = Modifier.padding(start = 92.dp, end = 20.dp, top = 12.dp).fillMaxWidth().height(60.dp))
        Row(Modifier.weight(1f).fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 16.dp, bottom = 8.dp)) {
            Vial(tilt.y, vertical = true, modifier = Modifier.width(60.dp).fillMaxHeight())
            Box(Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp), contentAlignment = Alignment.Center) {
                Bullseye(tilt, Modifier.fillMaxWidth().aspectRatio(1f))
            }
        }
        if (state.gravity == null) Text(stringResource(R.string.no_sensor), Modifier.align(Alignment.CenterHorizontally), color = LevelColors.Muted)
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Readout("X", Format.slope(tilt.x, state.unit), state.level, Modifier.weight(1f))
            Text(stringResource(R.string.calibrate), Modifier.clip(CircleShape).background(LevelColors.Accent).clickable(onClick = state::calibrate)
                .padding(horizontal = 18.dp, vertical = 12.dp), color = LevelColors.OnAccent, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            Readout("Y", Format.slope(tilt.y, state.unit), state.level, Modifier.weight(1f))
        }
    }
}

/** «Guardar medición» and «Mediciones guardadas», on every measuring screen. */
@Composable fun MeasureButtons(state: LevelState, actions: AppActions) {
    RoundButton(Icons.Outlined.BookmarkAdd, stringResource(R.string.save)) { if (state.save() != null) actions.toast(R.string.saved) }
    RoundButton(Icons.AutoMirrored.Outlined.List, stringResource(R.string.measurements)) { state.page = Page.MEASUREMENTS }
}

/** «X: 02,8°» in monospace; the accent when the phone is level. */
@Composable fun Readout(axis: String, value: String, level: Boolean, modifier: Modifier = Modifier) =
    Text("$axis: $value", modifier.clip(RoundedCornerShape(16.dp)).background(LevelColors.Card).padding(vertical = 12.dp),
        color = if (level) LevelColors.AccentText else LevelColors.Text, fontFamily = FontFamily.Monospace, fontSize = 18.sp,
        fontWeight = FontWeight.Bold, maxLines = 1, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
