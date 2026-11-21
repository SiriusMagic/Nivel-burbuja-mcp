package com.zirakusa.nivel.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.ui.theme.Accent
import com.zirakusa.nivel.ui.theme.LevelColors
import com.zirakusa.nivel.ui.theme.ThemeMode

/** A rounded card of settings. */
@Composable fun Group(content: @Composable ColumnScope.() -> Unit) =
    Column(Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(LevelColors.Card), content = content)

/** One line of a card: title, optional subtitle and an arrow when it opens something. */
@Composable fun Item(title: String, subtitle: String?, onClick: (() -> Unit)?) =
    Row(Modifier.fillMaxWidth().then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier).padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp)
            if (subtitle != null) Text(subtitle, color = LevelColors.Muted, fontSize = 14.sp)
        }
        if (onClick != null) Icon(Icons.AutoMirrored.Outlined.KeyboardArrowRight, null, tint = LevelColors.Muted)
    }

/** A line with a switch. */
@Composable fun SwitchItem(title: String, subtitle: String, on: Boolean, onChange: (Boolean) -> Unit) =
    Row(Modifier.fillMaxWidth().clickable { onChange(!on) }.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 17.sp)
            Text(subtitle, color = LevelColors.Muted, fontSize = 14.sp)
        }
        Switch(on, onChange, colors = SwitchDefaults.colors(checkedTrackColor = LevelColors.Accent, checkedThumbColor = LevelColors.OnAccent))
    }

/** «Temas»: Sistema, Claro, Oscuro or Negro, and the accent colour. */
@Composable fun ThemeCard(mode: ThemeMode, accent: Accent, onMode: (ThemeMode) -> Unit, onAccent: (Accent) -> Unit) = Group {
    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.themes), fontSize = 17.sp)
        Segmented(listOf(R.string.theme_system, R.string.theme_light, R.string.theme_dark, R.string.theme_black).map { stringResource(it) },
            mode.ordinal, Modifier.fillMaxWidth()) { onMode(ThemeMode.entries[it]) }
        Text(stringResource(R.string.accent), fontSize = 15.sp, color = LevelColors.Muted)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Accent.entries.forEach { a ->
                val name = stringResource(accentNames.getValue(a))
                Box(Modifier.size(40.dp).clip(CircleShape).background(a.color)
                    .border(3.dp, if (a == accent) LevelColors.Text else Color.Transparent, CircleShape)
                    .clickable(onClickLabel = name) { onAccent(a) }.semantics { contentDescription = name },
                    contentAlignment = Alignment.Center) { if (a == accent) Icon(Icons.Filled.Check, null, Modifier.size(20.dp), tint = a.ink) }
            }
        }
    }
}

private val accentNames = mapOf(
    Accent.LIMA to R.string.accent_lime, Accent.AMARILLO to R.string.accent_yellow, Accent.NARANJA to R.string.accent_orange,
    Accent.ROSA to R.string.accent_pink, Accent.AZUL to R.string.accent_blue, Accent.VERDE to R.string.accent_green,
)
