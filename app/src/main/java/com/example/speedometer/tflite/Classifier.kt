package com.example.speedometer.tflite

import android.content.res.AssetManager
import android.graphics.*
import android.util.Log
import com.example.speedometer.utils.ImageUtils
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel


class Classifier(assetManager: AssetManager, modelPath: String) {
    private var INTERPRETER: Interpreter
    init {
        val tfliteOptions = Interpreter.Options()
        tfliteOptions.setNumThreads(5)
        tfliteOptions.setUseNNAPI(true)
        INTERPRETER = Interpreter(loadModelFile(assetManager, modelPath),tfliteOptions)
    }

    private fun loadModelFile(assetManager: AssetManager, modelPath: String): MappedByteBuffer {
        val fileDescriptor = assetManager.openFd(modelPath)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun recognizeImage(bitmap: Bitmap): Float {
        //val scaledBitmap = Bitmap.createScaledBitmap(bitmap, INPUT_SIZE, INPUT_SIZE, false)
        //val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)
        val imageArray = ImageUtils.convertImageToFloatArray( bitmap )
        val result = Array(1) { FloatArray(1) }
        INTERPRETER.run(imageArray, result)
        return result[0][0]
    }

}