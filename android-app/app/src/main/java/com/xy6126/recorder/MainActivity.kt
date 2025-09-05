package com.xy6126.recorder

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.Surface
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : Activity(), SurfaceHolder.Callback {

    private var surface1: SurfaceView? = null
    private var surface2: SurfaceView? = null
    private var holder1: SurfaceHolder? = null
    private var holder2: SurfaceHolder? = null
    private var surface1Ready = false
    private var surface2Ready = false

    private var recorder1: MediaRecorder? = null
    private var recorder2: MediaRecorder? = null

    private var cameraDevice1: CameraDevice? = null
    private var cameraDevice2: CameraDevice? = null

    private var session1: CameraCaptureSession? = null
    private var session2: CameraCaptureSession? = null

    private val PERMISSION_REQUEST_CODE = 100

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        surface1 = SurfaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 1)
        }
        surface2 = SurfaceView(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, 1)
        }

        layout.addView(surface1)
        layout.addView(surface2)
        setContentView(layout)

        holder1 = surface1?.holder?.apply { addCallback(this@MainActivity) }
        holder2 = surface2?.holder?.apply { addCallback(this@MainActivity) }

        checkPermissions()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun checkPermissions() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )

        val needPermissions = permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (needPermissions.isEmpty()) {
            waitForSurfaces()
        } else {
            requestPermissions(permissions, PERMISSION_REQUEST_CODE)
        }
    }

    private fun waitForSurfaces() {
        Handler(Looper.getMainLooper()).postDelayed({
            if (surface1Ready && surface2Ready) {
                startRecording()
            } else {
                waitForSurfaces()
            }
        }, 100)
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        when (holder) {
            holder1 -> surface1Ready = true
            holder2 -> surface2Ready = true
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        when (holder) {
            holder1 -> surface1Ready = false
            holder2 -> surface2Ready = false
        }
    }

    private fun startRecording() {
        val manager = getSystemService(CAMERA_SERVICE) as CameraManager
        val cameraIds = manager.cameraIdList
        Log.d("Camera","camera available: $cameraIds")
        if (cameraIds.size < 2) {
            Log.e("XY6126", "Device has only ${cameraIds.size} camera(s)")
            openCamera(manager, cameraIds[0], 0, holder1!!.surface)
        } else {
            openCamera(manager, cameraIds[0], 0, holder1!!.surface)
            Handler(Looper.getMainLooper()).postDelayed({
                openCamera(manager, cameraIds[1], 1, holder2!!.surface)
                scheduleStop()
            }, 2000)
        }
    }

    private fun openCamera(manager: CameraManager, cameraId: String, camIndex: Int, previewSurface: Surface) {
        try {
            manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    if (camIndex == 0) {
                        cameraDevice1 = camera
                        setupRecorder(0)
                        createSession(camera, recorder1!!, previewSurface, camIndex)
                    } else {
                        cameraDevice2 = camera
                        setupRecorder(1)
                        createSession(camera, recorder2!!, previewSurface, camIndex)
                    }
                }

                override fun onDisconnected(camera: CameraDevice) {
                    camera.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    Log.e("XY6126","$error")
                    camera.close()
                }
            }, null)
        } catch (e: SecurityException) {
            Log.e("XY6126", "Camera permission error", e)
        }
    }

    private fun setupRecorder(camIndex: Int) {
        val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
        val file = File(getExternalFilesDir(Environment.DIRECTORY_MOVIES), "CAM${camIndex}_$timestamp.mp4")

        val recorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setOutputFile(file.absolutePath)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoSize(1280, 720)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(2_000_000)
            prepare()
        }

        if (camIndex == 0) recorder1 = recorder else recorder2 = recorder
    }

    private fun createSession(camera: CameraDevice, recorder: MediaRecorder, previewSurface: Surface, camIndex: Int) {
        val surfaces = listOf(previewSurface, recorder.surface)

        camera.createCaptureSession(surfaces, object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                val builder = camera.createCaptureRequest(CameraDevice.TEMPLATE_RECORD).apply {
                    addTarget(previewSurface)
                    addTarget(recorder.surface)
                }
                session.setRepeatingRequest(builder.build(), null, null)
                recorder.start()
                if (camIndex == 0) session1 = session else session2 = session
                Log.d("XY6126", "Camera $camIndex recording started")
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("XY6126", "Camera $camIndex configuration failed")
            }
        }, null)
    }

    private fun scheduleStop() {
        Handler(Looper.getMainLooper()).postDelayed({
            stopRecording()
//            Handler(Looper.getMainLooper()).postDelayed({
//                startRecording()
//                scheduleStop()
//            },2000
//
//            )

        }, 30000) // 30 seconds
    }

    private fun stopRecording() {
        try {
            recorder1?.let {
                try { it.stop() } catch (e: Exception) {
                    Log.e("XY6126", "Error stopping cam recorder 1", e)
                }
                try { it.release() } catch (e: Exception) {
                    Log.e("XY6126", "Error releasing cam recorder 1", e)
                }
            }
            recorder1 = null

            recorder2?.let {
                try { it.stop() } catch (e: Exception) {
                    Log.e("XY6126", "Error stopping cam recorder 2", e)
                }
                try { it.release() } catch (e: Exception) {
                    Log.e("XY6126", "Error releasing cam recorder 2", e)
                }
            }
            recorder2 = null

            cameraDevice1?.close()
            cameraDevice1 = null
            cameraDevice2?.close()
            cameraDevice2 = null

            session1?.close()
            session1 = null
            session2?.close()
            session2 = null

            Log.d("XY6126", "Recordings stopped")
        } catch (e: Exception) {
            Log.e("XY6126", "Error stopping recording", e)
        }
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                waitForSurfaces()
            } else {
                finish()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}