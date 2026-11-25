package com.zirakusa.nivel

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.SideEffect
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.WindowCompat
import com.zirakusa.nivel.data.SharedValues
import com.zirakusa.nivel.mcp.McpMode
import com.zirakusa.nivel.ui.AppActions
import com.zirakusa.nivel.ui.LevelState
import com.zirakusa.nivel.ui.NivelApp
import com.zirakusa.nivel.ui.Page
import com.zirakusa.nivel.ui.Tab
import com.zirakusa.nivel.ui.theme.LevelColors
import com.zirakusa.nivel.ui.theme.LevelTheme

/** Creates the state, feeds it the sensor while on screen, and does what the screens ask (torch, camera, beep, copy). */
class MainActivity : ComponentActivity() {
    private lateinit var state: LevelState
    private lateinit var sensor: TiltSensor
    private lateinit var torch: Torch
    private val beeper by lazy { Beeper() }
    private var refusals = 0
    private val camera = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        state.cameraAllowed = granted
        if (!granted) refusals++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppIcon.load(this)
        enableEdgeToEdge()
        state = LevelState(SharedValues(this), onLevel = { beeper.beep() })
        torch = Torch(this)
        @Suppress("DEPRECATION") // display is API 30+; the window manager's default display works on every version.
        sensor = TiltSensor(this, { windowManager.defaultDisplay.rotation }) { x, y, z, r -> runOnUiThread { state.onGravity(x, y, z, r) } }
        McpMode.load(this)
        val actions = AppActions(hasFlash = torch.available, torch = ::setTorch, askCamera = ::askCamera, toast = ::toast, openArka = ::openArka)
        setContent {
            LevelTheme(state.themeMode, state.accent) {
                val light = LevelColors.isLight && state.tab != Tab.CAMERA
                val keepOn = state.keepScreenOn
                // The camera's preview needs the camera, so the flashlight goes off there.
                if (state.tab == Tab.CAMERA && state.torch) SideEffect { setTorch(false) }
                SideEffect {
                    WindowCompat.getInsetsController(window, window.decorView).run { isAppearanceLightStatusBars = light; isAppearanceLightNavigationBars = light }
                    if (keepOn) window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) else window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
                }
                BackHandler(state.page != Page.MAIN || state.tab != Tab.LEVEL) {
                    if (state.page != Page.MAIN) state.page = Page.MAIN else state.tab = Tab.LEVEL
                }
                NivelApp(state, actions) { CameraPreview(it) }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        state.cameraAllowed = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        sensor.start()
    }

    override fun onStop() {
        super.onStop()
        sensor.stop()
        if (state.torch) setTorch(false)
    }

    override fun onDestroy() { super.onDestroy(); beeper.release() }

    private fun setTorch(on: Boolean) { if (torch.set(on)) state.torch = on }

    /** Asks for the camera; after two refusals Android stops asking, so the app's page in the phone's settings opens. */
    private fun askCamera() {
        if (refusals >= 2) startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, "package:$packageName".toUri()))
        else camera.launch(Manifest.permission.CAMERA)
    }

    private fun toast(text: Int) = Toast.makeText(this, text, Toast.LENGTH_SHORT).show()

    /** «Modo MCP» in Ajustes: it is turned on, allowed on the phone and given tokens in Arka, so this opens Arka. */
    private fun openArka() {
        val open = packageManager.getLaunchIntentForPackage("com.zirakusa.gatewayhub")
        if (open != null) startActivity(open) else Toast.makeText(this, R.string.mcp_no_arka, Toast.LENGTH_LONG).show()
    }
}
