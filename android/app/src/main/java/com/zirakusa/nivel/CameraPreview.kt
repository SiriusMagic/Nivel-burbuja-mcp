package com.zirakusa.nivel

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.os.HandlerThread
import android.util.Size
import android.view.Surface
import android.view.TextureView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import kotlin.math.abs
import kotlin.math.max

/**
 * «Cámara»: the back camera's live picture with the platform's Camera2 and a TextureView (no extra library). Nothing
 * is recorded: the frames only go to the screen. Opened while the screen is shown, closed when it goes.
 */
@Composable fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val camera = remember { BackCamera(context) }
    DisposableEffect(Unit) { onDispose { camera.close() } }
    AndroidView({ TextureView(it).also(camera::attach) }, modifier)
}

private class BackCamera(private val context: Context) : TextureView.SurfaceTextureListener {
    private val manager = context.getSystemService(CameraManager::class.java)
    private val thread = HandlerThread("camera").apply { start() }
    private val handler = Handler(thread.looper)
    private var device: CameraDevice? = null
    private var session: CameraCaptureSession? = null
    private var view: TextureView? = null
    private var closed = false

    fun attach(texture: TextureView) {
        view = texture
        texture.surfaceTextureListener = this
        if (texture.isAvailable) open(texture.surfaceTexture ?: return, texture.width, texture.height)
    }

    private fun open(texture: SurfaceTexture, width: Int, height: Int) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) return
        val id = manager?.cameraIdList?.firstOrNull {
            manager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        } ?: return
        val sizes = manager.getCameraCharacteristics(id).get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            ?.getOutputSizes(SurfaceTexture::class.java).orEmpty()
        val size = sizes.filter { it.width <= 1920 && it.height <= 1080 }.minByOrNull { abs(it.width * 9 - it.height * 16) * 10_000 - it.width }
            ?: Size(1280, 720)
        texture.setDefaultBufferSize(size.width, size.height)
        fit(width, height, size)
        try {
            manager.openCamera(id, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    if (closed) { camera.close(); return }
                    device = camera
                    start(camera, Surface(texture))
                }
                override fun onDisconnected(camera: CameraDevice) { camera.close(); device = null }
                override fun onError(camera: CameraDevice, error: Int) { camera.close(); device = null }
            }, handler)
        } catch (_: CameraAccessException) { } catch (_: SecurityException) { }
    }

    @Suppress("DEPRECATION") // createCaptureSession(List) is the simple way that works from API 26 up.
    private fun start(camera: CameraDevice, surface: Surface) = try {
        val request = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(surface) }.build()
        camera.createCaptureSession(listOf(surface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(s: CameraCaptureSession) {
                session = s
                try { s.setRepeatingRequest(request, null, handler) } catch (_: CameraAccessException) { } catch (_: IllegalStateException) { }
            }
            override fun onConfigureFailed(s: CameraCaptureSession) = Unit
        }, handler)
    } catch (_: CameraAccessException) { } catch (_: IllegalStateException) { }

    /** Fills the view without stretching (centre crop): the camera's frames arrive turned to portrait. */
    private fun fit(width: Int, height: Int, size: Size) {
        val texture = view ?: return
        if (width == 0 || height == 0) return
        val bufferW = size.height.toFloat(); val bufferH = size.width.toFloat()
        val scale = max(width / bufferW, height / bufferH)
        texture.setTransform(Matrix().apply { setScale(bufferW * scale / width, bufferH * scale / height, width / 2f, height / 2f) })
    }

    fun close() {
        closed = true
        session?.close(); session = null
        device?.close(); device = null
        thread.quitSafely()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) = open(surface, width, height)
    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) = Unit
    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean { close(); return true }
    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) = Unit
}
