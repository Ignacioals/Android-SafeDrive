package com.example.speedometer

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.Image
import android.media.ImageReader
import android.os.*
import android.util.Log
import android.util.Size
import android.view.Surface
import android.view.View
import android.view.WindowManager
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.example.speedometer.utils.ImageUtils
import kotlin.math.absoluteValue


/**
 * Copyright (c) 2019 by Roman Sisik. All rights reserved.
 */
/*
class BackCamService: Service() {
    private var rgbFrameBitmap: Bitmap? = null
    private var rgbBytes: IntArray? = null
    private var isProcessingFrame = false
    private val yuvBytes = arrayOfNulls<ByteArray>(3)
    private var yRowStride: Int = 0
    private var imageConverter: Runnable? = null
    private lateinit var classifier: Classifier

    // UI
    private var wm: WindowManager? = null

    // Camera2-related stuff
    private var cameraManager: CameraManager? = null
    private var previewSize: Size? = null
    private var cameraDevice: CameraDevice? = null
    private var captureRequest: CaptureRequest? = null
    private var captureSession: CameraCaptureSession? = null
    private var imageReader: ImageReader? = null



    /**
     * An additional thread for running tasks that shouldn't block the UI.
     */
    private var backgroundThread: HandlerThread? = null

    /**
     * A [Handler] for running tasks in the background.
     */
    private var backgroundHandler: Handler? = null


    private val captureCallback = object : CameraCaptureSession.CaptureCallback() {

        override fun onCaptureProgressed(
                session: CameraCaptureSession,
                request: CaptureRequest,
                partialResult: CaptureResult
        ) {}

        override fun onCaptureCompleted(
                session: CameraCaptureSession,
                request: CaptureRequest,
                result: TotalCaptureResult
        ) {}
    }
    var image: Image? = null
    private val imageListener = ImageReader.OnImageAvailableListener { reader ->

        try {

            if (!isProcessingFrame) {
                isProcessingFrame = true
                image = reader?.acquireLatestImage()
                if (image != null && image!!.width > 0 && image!!.height > 0) {
                    Log.d(TAG, "Got image: " + image!!.width + " x " + image!!.height)
                    if (rgbBytes == null) {
                        rgbBytes = IntArray(image!!.width * image!!.height)
                    }
                    // Process image here..ideally async so that you don't block the callback
                    // ..
                    Trace.beginSection("imageAvailable")
                    val planes = image!!.getPlanes()
                    fillBytes(planes, yuvBytes)
                    yRowStride = planes[0].getRowStride()
                    val uvRowStride = planes[1].getRowStride()
                    val uvPixelStride = planes[1].getPixelStride()

                    imageConverter = Runnable {
                        ImageUtils.convertYUV420ToARGB8888(
                                yuvBytes[0],
                                yuvBytes[1],
                                yuvBytes[2],
                                image!!.width,
                                image!!.height,
                                yRowStride,
                                uvRowStride,
                                uvPixelStride,
                                rgbBytes
                        )
                    }

                    rgbFrameBitmap?.setPixels(
                            getRgbBytes(), 0, image!!.width, 0, 0, image!!.width,
                            image!!.height
                    )
                    var steeringWheelDetected = rgbFrameBitmap?.let { it1 -> classifier.steeringWheelDetected(it1) }


                    if (steeringWheelDetected !== null && steeringWheelDetected) {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            MainActivity.onVolante.visibility = View.VISIBLE
                        })
                    } else {
                        Handler(Looper.getMainLooper()).post(Runnable {
                            MainActivity.onVolante.visibility = View.INVISIBLE
                        })
                    }


//                    var results = rgbFrameBitmap?.let { it1 -> classifier.recognizeImage(it1) };
//                    Log.d("results", results.toString())
                    //activity?.runOnUiThread({
                    //    itemNameTextView.setText(results?.get(0)?.title)
                    //    confidenceTextView.setText(String.format("%.2f",(results?.get(0)?.confidence?.times(100)))+"%");
                    //})

                }
                isProcessingFrame = false
            }
        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        } finally {
            image?.close()
            Trace.endSection()
        }

    }
    protected fun fillBytes(planes: Array<Image.Plane>, yuvBytes: Array<ByteArray?>) {
        // Because of the variable row stride it's not possible to know in
        // advance the actual necessary dimensions of the yuv planes.
        for (i in planes.indices) {
            val buffer = planes[i].buffer
            if (yuvBytes[i] == null) {
//                LOGGER.d("Initializing buffer %d at size %d", i, buffer.capacity())
                yuvBytes[i] = ByteArray(buffer.capacity())
            }
            buffer.get(yuvBytes[i])
        }
    }
    private fun getRgbBytes(): IntArray? {
        try {
            imageConverter?.run()

        } catch (e: Exception) {
            Log.e("Exception", e.toString())
        }
        return rgbBytes
    }
    private val stateCallback = object : CameraDevice.StateCallback() {

        override fun onOpened(currentCameraDevice: CameraDevice) {
            cameraDevice = currentCameraDevice
            createCaptureSession()
        }

        override fun onDisconnected(currentCameraDevice: CameraDevice) {
            currentCameraDevice.close()
            cameraDevice = null
        }

        override fun onError(currentCameraDevice: CameraDevice, error: Int) {
            currentCameraDevice.close()
            cameraDevice = null
        }
    }
    /**
     * Starts a background thread and its [Handler].
     */
    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    /**
     * Stops the background thread and its [Handler].
     */
    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Log.e(TAG, e.toString())
        }

    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

//        when(intent?.action) {
//            ACTION_START -> start()
//
//            ACTION_START_WITH_PREVIEW -> startWithPreview()
//        }
        startBackgroundThread()

        start()

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onCreate() {
        super.onCreate()
        val mModelPath = "wheel_model_20px_10epochs.tflite"
        classifier = Classifier(assets, mModelPath, this);
        startForeground()
    }

    override fun onDestroy() {
        super.onDestroy()

        stopCamera()


        sendBroadcast(Intent(ACTION_STOPPED))
    }

    private fun start() {

        initCam(320, 240)
    }

    private fun initCam(width: Int, height: Int) {

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        var camId: String? = null

        for (id in cameraManager!!.cameraIdList) {
            val characteristics = cameraManager!!.getCameraCharacteristics(id)
            val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
            if (facing == CameraCharacteristics.LENS_FACING_BACK) {
                camId = "1"
                break
            }
        }
        if(camId == null) {
            camId = cameraManager!!.cameraIdList[1]
        }

//        previewSize = chooseSupportedSize(camId!!, width, height)

        previewSize = Size(width, height);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        if (camId != null) {
            cameraManager!!.openCamera(camId, stateCallback, backgroundHandler)
        }
    }
    private fun startForeground() {

        val pendingIntent: PendingIntent =
                Intent(this, MainActivity::class.java).let { notificationIntent ->
                    PendingIntent.getActivity(this, 0, notificationIntent, 0)
                }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_NONE)
            channel.lightColor = Color.BLUE
            channel.lockscreenVisibility = Notification.VISIBILITY_PRIVATE
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getText(R.string.app_name))
                .setContentText(getText(R.string.app_name))
                .setSmallIcon(R.drawable.notification_template_icon_bg)
                .setContentIntent(pendingIntent)
                .setTicker(getText(R.string.app_name))
                .build()

        startForeground(ONGOING_NOTIFICATION_ID, notification)
    }

    private fun createCaptureSession() {
        try {
            // Prepare surfaces we want to use in capture session
            val targetSurfaces = ArrayList<Surface>()

            // Prepare CaptureRequest that can be used with CameraCaptureSession
            val requestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW).apply {

                // Configure target surface for background processing (ImageReader)
                imageReader = ImageReader.newInstance(
                        previewSize!!.width, previewSize!!.height,
                        ImageFormat.YUV_420_888, 2
                )
                imageReader!!.setOnImageAvailableListener(imageListener, backgroundHandler)
                rgbFrameBitmap = Bitmap.createBitmap(previewSize!!.width, previewSize!!.height, Bitmap.Config.ARGB_8888)

                targetSurfaces.add(imageReader!!.surface)
                addTarget(imageReader!!.surface)

                // Set some additional parameters for the request
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
            }

            // Prepare CameraCaptureSession
            cameraDevice!!.createCaptureSession(targetSurfaces,
                    object : CameraCaptureSession.StateCallback() {

                        override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                            // The camera is already closed
                            if (null == cameraDevice) {
                                return
                            }

                            captureSession = cameraCaptureSession
                            try {
                                // Now we can start capturing
                                captureRequest = requestBuilder!!.build()
                                captureSession!!.setRepeatingRequest(captureRequest!!, captureCallback, backgroundHandler)

                            } catch (e: CameraAccessException) {
                                Log.e(TAG, "createCaptureSession", e)
                            }

                        }

                        override fun onConfigureFailed(cameraCaptureSession: CameraCaptureSession) {
                            Log.e(TAG, "createCaptureSession()")
                        }
                    }, null
            )
        } catch (e: CameraAccessException) {
            Log.e(TAG, "createCaptureSession", e)
        }
    }

    private fun stopCamera() {
        try {
            captureSession?.close()
            captureSession = null

            cameraDevice?.close()
            cameraDevice = null

            imageReader?.close()
            imageReader = null

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    companion object {

        val TAG = "CamService"

        val ACTION_START = "eu.sisik.backgroundcam.action.START"
        val ACTION_START_WITH_PREVIEW = "eu.sisik.backgroundcam.action.START_WITH_PREVIEW"
        val ACTION_STOPPED = "eu.sisik.backgroundcam.action.STOPPED"

        val ONGOING_NOTIFICATION_ID = 6660
        val CHANNEL_ID = "cam_service_channel_id"
        val CHANNEL_NAME = "cam_service_channel_name"

    }
} */