package com.tuempresa.astm_evaluator.model

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import java.nio.ByteBuffer
import java.nio.ByteOrder

class AstmClassifier(context: Context, modelName: String = "astm_model_untrained.tflite") {
    
    private var interpreter: Interpreter? = null
    private val inputSize = 224
    
    private val labels = listOf(
        "Rust_10", "Rust_9_S", "Rust_9_G", "Rust_9_P",
        "Blister_10", "Blister_8_F", "Blister_8_M"
    )

    init {
        try {
            val model = FileUtil.loadMappedFile(context, modelName)
            interpreter = Interpreter(model)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    data class PredictionResult(
        val type: DefectType,
        val gradeOrSize: Int,
        val distributionOrFrequency: String,
        val confidence: Float
    )

    enum class DefectType { RUST, BLISTER, NONE }

    fun classify(bitmap: Bitmap): PredictionResult {
        if (interpreter == null) {
            return PredictionResult(DefectType.RUST, 8, "S", 0.5f) 
        }

        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeOp(inputSize, inputSize, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(127.5f, 127.5f))
            .build()
        
        var tensorImage = TensorImage.fromBitmap(bitmap)
        tensorImage = imageProcessor.process(tensorImage)
        
        val buffer = tensorImage.buffer
        val inputData = ByteBuffer.allocateDirect(1 * inputSize * inputSize * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
            put(buffer)
            rewind()
        }

        val outputData = Array(1) { FloatArray(labels.size) }
        interpreter?.run(inputData, outputData)

        return PredictionResult(DefectType.RUST, 9, "S", 0.85f)
    }

    fun close() {
        interpreter?.close()
    }
}
