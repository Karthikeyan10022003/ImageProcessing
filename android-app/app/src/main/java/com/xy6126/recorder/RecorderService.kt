package com.xy6126.recorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import android.view.Surface
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class RecorderService : Service() {

    private var recorder1: MediaRecorder? = null
    private var recorder2: MediaRecorder? = null
    private var cameraDevice1: CameraDevice? = null
    private var cameraDevice2: CameraDevice? = null
    private var session1: CameraCaptureSession? = null
    private var session2: CameraCaptureSession? = null

    private var surfaceA: Surface? = null
    private var surfaceB: Surface? = null

    override fun onBind(intent: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()

        // ✅ Foreground notification (required for long-running background recording)
        val channel = NotificationChannel(
            "recorder_channel",
            "Recorder Service",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)

        val notification = Notification.Builder(this, "recorder_channel")
            .setContentTitle("Recording in progress")
            .setContentText("Capturing video from cameras")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()

        startForeground(1, notification)

        // ✅ Create off-screen surfaces (no WindowManager, no crash)
        surfaceA = Surface(SurfaceTexture(10).apply { setDefaultBufferSize(1280, 720) })
        surfaceB = Surface(SurfaceTexture(11).apply { setDefaultBufferSize(1280, 720) })

        startRecording()
    }

    private fun startRecording() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraIds = manager.cameraIdList
        Log.d("XY6126", "Cameras available: $cameraIds")

        if (cameraIds.isEmpty()) {
            Log.e("XY6126", "❌ No camera found")
            return
        }

        if (cameraIds.size < 2) {
            openCamera(manager, cameraIds[0], 0, surfaceA!!)
        } else {
            openCamera(manager, cameraIds[0], 0, surfaceA!!)
            Handler(Looper.getMainLooper()).postDelayed({
                openCamera(manager, cameraIds[1], 1, surfaceB!!)
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
                    Log.e("XY6126", "Camera $camIndex error: $error")
                    camera.close()
                }
            }, Handler(Looper.getMainLooper()))
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
                Log.d("XY6126", "✅ Camera $camIndex recording started")
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("XY6126", "❌ Camera $camIndex configuration failed")
            }
        }, Handler(Looper.getMainLooper()))
    }

    private fun scheduleStop() {
        Handler(Looper.getMainLooper()).postDelayed({
            stopRecording()
            Handler(Looper.getMainLooper()).postDelayed({
                startRecording()
                scheduleStop()
            }, 2000)
        }, 30000) // stop every 30s, restart after 2s
    }

    private fun stopRecording() {
        try {
            recorder1?.let {
                try { it.stop() } catch (e: Exception) { Log.e("XY6126", "Stop cam1", e) }
                try { it.release() } catch (e: Exception) { Log.e("XY6126", "Release cam1", e) }
            }
            recorder1 = null

            recorder2?.let {
                try { it.stop() } catch (e: Exception) { Log.e("XY6126", "Stop cam2", e) }
                try { it.release() } catch (e: Exception) { Log.e("XY6126", "Release cam2", e) }
            }
            recorder2 = null

            cameraDevice1?.close(); cameraDevice1 = null
            cameraDevice2?.close(); cameraDevice2 = null

            session1?.close(); session1 = null
            session2?.close(); session2 = null

            Log.d("XY6126", "✅ Recordings stopped")
        } catch (e: Exception) {
            Log.e("XY6126", "Error stopping recording", e)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopRecording()
    }
}
