package com.example.speedometer

import android.content.Context
import android.content.res.AssetManager
import android.graphics.Bitmap
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import com.example.speedometer.utils.ImageUtils
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.FaceDetector
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(assetManager: AssetManager, modelPath: String, context: Context) {
    private var INTERPRETER: Interpreter
    private var detector: FaceDetector
    init {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(5)
        tfliteOptions.setUseNNAPI(true)
        INTERPRETER = Interpreter(loadModelFile(assetManager, modelPath),tfliteOptions)

//        detector = FaceDetector.Builder(context)
//                .setTrackingEnabled(true)
//                .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
//                .setMode(FaceDetector.FAST_MODE)
//                .build()
         detector = FaceDetector.Builder(context)
                .setTrackingEnabled(false)
                 .setClassificationType(FaceDetector.ALL_CLASSIFICATIONS)
                 .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                .build()
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun isDriver(bitmap: Bitmap): Boolean {
        checkEyes(bitmap)
        //val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        //val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val imageArray = ImageUtils.convertImageToFloatArray( bitmap )
        val result = Array(1) { FloatArray(1) }
        INTERPRETER.run(imageArray, result)

        Log.d("results", result[0][0].toString())
        return result[0][0] < 0.009;
    }

    fun isPassenger(bitmap: Bitmap): Boolean {
        checkEyes(bitmap)
        //val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        //val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val imageArray = ImageUtils.convertImageToFloatArray( bitmap )
        val result = Array(1) { FloatArray(1) }
        INTERPRETER.run(imageArray, result)

        Log.d("results", result[0][0].toString())
        return result[0][0] > 0.7;
    }

    fun steeringWheelDetected(bitmap: Bitmap): Boolean {
        checkEyes(bitmap)
        //val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        //val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val imageArray = ImageUtils.convertImageToFloatArray( bitmap )
        val result = Array(1) { FloatArray(1) }
        INTERPRETER.run(imageArray, result)

        Log.d("results", result[0][0].toString())
        return result[0][0] > 0.7;
    }

    private val THRESHOLD = 0.7f

    private fun checkEyes(bitmap: Bitmap) {
        val outputFrame: Frame = Frame.Builder().setBitmap(bitmap).build()
        val faces = detector.detect(outputFrame)
        if (faces != null && faces[0] != null) {

            if (MainActivity.p == 0) {
                if (faces[0].isLeftEyeOpenProbability > THRESHOLD || faces[0].isRightEyeOpenProbability > THRESHOLD) {
                    if (!MainActivity.eyeDetected) {
                        Handler(Looper.getMainLooper()).post(Runnable {

                            MainActivity.eyeDetected = true
                            MainActivity.onOjos.visibility = View.VISIBLE
                        })

                    }
                } else {
                    Handler(Looper.getMainLooper()).post(Runnable {
                        MainActivity.eyeDetected = false
                        MainActivity.onOjos.visibility = View.INVISIBLE
                    })
                }
            }
        }
        else{
            Handler(Looper.getMainLooper()).post(Runnable {
                MainActivity.eyeDetected = false
                MainActivity.onOjos.visibility = View.INVISIBLE
            })
        }


    }

}