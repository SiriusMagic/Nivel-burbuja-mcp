package com.zirakusa.nivel.ui.screens

import com.zirakusa.nivel.IconCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.BuildConfig
import com.zirakusa.nivel.R
import com.zirakusa.nivel.data.SlopeUnit
import com.zirakusa.nivel.mcp.McpMode
import com.zirakusa.nivel.mcp.RelayLink
import com.zirakusa.nivel.ui.AppActions
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.Page
import com.zirakusa.nivel.ui.theme.LevelColors

/** «Ajustes»: calibration, screen, unit, sound, Temas, saved measurements, Modo MCP, privacy and version. */
@Composable fun SettingsScreen(state: LevelState, actions: AppActions) {
    var privacy by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize().background(LevelColors.Background).statusBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text(stringResource(R.string.settings), Modifier.padding(start = 4.dp, top = 8.dp), fontSize = 28.sp)
        Group {
            Item(stringResource(R.string.reset_calibration), stringResource(R.string.reset_calibration_hint)) {
                state.resetCalibration(); actions.toast(R.string.reset_done)
            }
            HorizontalDivider(color = LevelColors.Chip)
            SwitchItem(stringResource(R.string.keep_screen_on), stringResource(R.string.keep_screen_on_hint), state.keepScreenOn, state::changeKeepScreenOn)
            HorizontalDivider(color = LevelColors.Chip)
            SwitchItem(stringResource(R.string.sound), stringResource(R.string.sound_hint), state.sound, state::changeSound)
        }
        Group {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(stringResource(R.string.slope_unit), fontSize = 17.sp)
                Segmented(listOf(R.string.unit_degrees, R.string.unit_percent, R.string.unit_mm).map { stringResource(it) }, state.unit.ordinal,
                    Modifier.fillMaxWidth()) { state.changeUnit(SlopeUnit.entries[it]) }
            }
        }
        IconCard(LevelColors.Card, LevelColors.Text, LevelColors.Muted, LevelColors.Accent)
        ThemeCard(state.themeMode, state.accent, state::changeTheme, state::changeAccent)
        Group {
            Item(stringResource(R.string.measurements), state.measurements.size.toString()) { state.page = Page.MEASUREMENTS }
            HorizontalDivider(color = LevelColors.Chip)
            val status = when {
                !McpMode.active -> stringResource(R.string.mcp_status_off)
                McpMode.state == RelayLink.State.ONLINE -> stringResource(R.string.mcp_online)
                McpMode.state == RelayLink.State.CONNECTING -> stringResource(R.string.mcp_connecting)
                else -> stringResource(R.string.mcp_offline)
            }
            Item(stringResource(R.string.mcp_title), "$status · ${stringResource(R.string.mcp_settings_hint)}", onClick = actions.openArka)
        }
        Group {
            Item(stringResource(R.string.privacy), null) { privacy = true }
            HorizontalDivider(color = LevelColors.Chip)
            Item(stringResource(R.string.about), stringResource(R.string.version, BuildConfig.VERSION_NAME), null)
        }
    }
    if (privacy) AlertDialog({ privacy = false }, confirmButton = { TextButton({ privacy = false }) { Text(stringResource(R.string.done)) } },
        title = { Text(stringResource(R.string.privacy)) }, text = { Text(stringResource(R.string.privacy_text)) })
}
