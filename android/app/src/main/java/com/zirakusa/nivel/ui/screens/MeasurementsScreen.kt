package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.Format
import com.zirakusa.nivel.data.Measurement
import com.zirakusa.nivel.data.Mode
import java.text.DateFormat
import java.util.Date
import com.zirakusa.nivel.ui.theme.LevelColors

/** «Mediciones guardadas»: newest first, each with its mode, angles and date, and a button to delete it. */
@Composable fun MeasurementsScreen(list: List<Measurement>, onDelete: (Long) -> Unit, onStart: () -> Unit, onBack: () -> Unit) =
    Column(Modifier.fillMaxSize().background(LevelColors.Background).systemBarsPadding()) {
        TopRow(stringResource(R.string.measurements), left = {
            RoundButton(Icons.AutoMirrored.Outlined.ArrowBack, stringResource(R.string.back), onClick = onBack)
        })
        if (list.isEmpty()) Column(Modifier.fillMaxSize().padding(32.dp), verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Outlined.Straighten, null, Modifier.size(56.dp), tint = LevelColors.Muted)
            Text(stringResource(R.string.empty_title), fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.empty_text), color = LevelColors.Muted, textAlign = TextAlign.Center, fontSize = 16.sp)
            Button(onStart, Modifier.padding(top = 8.dp), colors = ButtonDefaults.buttonColors(LevelColors.Accent, LevelColors.OnAccent)) {
                Text(stringResource(R.string.start), fontSize = 16.sp)
            }
        } else LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(list, key = { it.id }) { MeasurementRow(it, onDelete) }
        }
    }

@Composable private fun MeasurementRow(m: Measurement, onDelete: (Long) -> Unit) =
    Row(Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(LevelColors.Card).padding(start = 18.dp, top = 12.dp, bottom = 12.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(stringResource(when (m.mode) { Mode.LEVEL -> R.string.mode_level; Mode.PLUMB -> R.string.mode_plumb; Mode.INCLINE -> R.string.mode_incline }),
                fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
            Text(when (m.mode) {
                Mode.LEVEL -> "X: ${Format.degrees(m.x)}   Y: ${Format.degrees(m.y)}"
                Mode.PLUMB -> Format.angle(m.x)
                Mode.INCLINE -> "${Format.angle(m.x)}   < ${Format.angle(m.y)} >"
            }, fontFamily = FontFamily.Monospace, fontSize = 16.sp, color = LevelColors.AccentText)
            Text(DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(m.at)), color = LevelColors.Muted, fontSize = 13.sp)
        }
        IconButton({ onDelete(m.id) }) { Icon(Icons.Outlined.DeleteOutline, stringResource(R.string.delete), tint = LevelColors.Muted) }
    }
