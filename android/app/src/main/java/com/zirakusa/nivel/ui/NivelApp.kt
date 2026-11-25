package com.zirakusa.nivel.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Adjust
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.zirakusa.nivel.R
import com.zirakusa.nivel.ui.screens.CameraScreen
import com.zirakusa.nivel.ui.screens.InclineScreen
import com.zirakusa.nivel.ui.screens.LevelScreen
import com.zirakusa.nivel.ui.screens.MeasurementsScreen
import com.zirakusa.nivel.ui.screens.PlumbScreen
import com.zirakusa.nivel.ui.screens.RulerScreen
import com.zirakusa.nivel.ui.screens.SettingsScreen
import com.zirakusa.nivel.ui.theme.LevelColors

/** What the screens ask of the phone (the activity gives the real ones; tests leave the defaults). */
class AppActions(
    val hasFlash: Boolean = true,
    val torch: (Boolean) -> Unit = {},
    val askCamera: () -> Unit = {},
    val toast: (Int) -> Unit = {},
    val copy: (String) -> Unit = {},
    val openArka: () -> Unit = {},
)

/** The whole app: one tab with the bottom bar, or a page over it (Modo MCP, Mediciones guardadas). */
@Composable fun NivelApp(state: LevelState, actions: AppActions = AppActions(), preview: @Composable (Modifier) -> Unit = {}) {
    when (state.page) {
        Page.MEASUREMENTS -> return MeasurementsScreen(state.measurements, state::delete,
            onStart = { state.page = Page.MAIN; state.tab = Tab.LEVEL }) { state.page = Page.MAIN }
        Page.MAIN -> Unit
    }
    if (state.tab == Tab.CAMERA) return CameraScreen(state, actions, preview)
    Column(Modifier.fillMaxSize().background(LevelColors.Background)) {
        Box(Modifier.weight(1f).fillMaxWidth()) {
            when (state.tab) {
                Tab.LEVEL -> LevelScreen(state, actions)
                Tab.PLUMB -> PlumbScreen(state, actions)
                Tab.INCLINE -> InclineScreen(state, actions)
                Tab.RULER -> RulerScreen(state)
                Tab.SETTINGS, Tab.CAMERA -> SettingsScreen(state, actions)
            }
        }
        BottomBar(state.tab) { state.tab = it }
    }
}

private val tabs = listOf(
    Triple(Tab.LEVEL, Icons.Outlined.Adjust, R.string.tab_level),
    Triple(Tab.PLUMB, Icons.Outlined.ArrowDownward, R.string.tab_plumb),
    Triple(Tab.INCLINE, Icons.Outlined.Architecture, R.string.tab_incline),
    Triple(Tab.RULER, Icons.Outlined.Straighten, R.string.tab_ruler),
    Triple(Tab.CAMERA, Icons.Outlined.PhotoCamera, R.string.tab_camera),
    Triple(Tab.SETTINGS, Icons.Outlined.Settings, R.string.settings),
)

@Composable private fun BottomBar(selected: Tab, onSelect: (Tab) -> Unit) =
    Row(Modifier.fillMaxWidth().background(LevelColors.Card).navigationBarsPadding().height(68.dp), verticalAlignment = Alignment.CenterVertically) {
        tabs.forEach { (tab, icon, label) -> TabButton(icon, stringResource(label), tab == selected, Modifier.weight(1f)) { onSelect(tab) } }
    }

@Composable private fun TabButton(icon: ImageVector, label: String, on: Boolean, modifier: Modifier, onClick: () -> Unit) =
    Column(modifier.clickable(onClick = onClick).padding(vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.width(52.dp).height(30.dp).clip(CircleShape).background(if (on) LevelColors.Accent else LevelColors.Card),
            contentAlignment = Alignment.Center) { Icon(icon, null, Modifier.size(22.dp), tint = if (on) LevelColors.OnAccent else LevelColors.Muted) }
        Text(label, Modifier.padding(top = 3.dp), fontSize = 11.sp, maxLines = 1, color = if (on) LevelColors.Text else LevelColors.Muted,
            fontWeight = if (on) FontWeight.SemiBold else FontWeight.Normal)
    }
