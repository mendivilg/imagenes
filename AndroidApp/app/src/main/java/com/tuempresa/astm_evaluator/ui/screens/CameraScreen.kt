package com.tuempresa.astm_evaluator.ui.screens

import android.content.Context
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.tuempresa.astm_evaluator.data.PreferencesManager
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraScreen(
    prefsManager: PreferencesManager,
    onImageCaptured: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val context = LocalContext.current
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val executor = remember { Executors.newSingleThreadExecutor() }
    
    val panelWidth = prefsManager.panelWidth
    val panelHeight = prefsManager.panelHeight
    val aspectRatio = panelWidth / panelHeight

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            update = { previewView ->
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    
                    try {
                        cameraProvider.unbindAll()
                        cameraProvider.bindToLifecycle(
                            context as androidx.lifecycle.LifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }, ContextCompat.getMainExecutor(context))
            },
            modifier = Modifier.fillMaxSize()
        )

        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            val rectWidth = if (aspectRatio > 1) canvasWidth * 0.8f else (canvasHeight * 0.8f) * aspectRatio
            val rectHeight = if (aspectRatio > 1) (canvasWidth * 0.8f) / aspectRatio else canvasHeight * 0.8f
            
            val left = (canvasWidth - rectWidth) / 2
            val top = (canvasHeight - rectHeight) / 2
            
            drawRect(
                color = Color.Green,
                topLeft = androidx.compose.ui.geometry.Offset(left, top),
                size = androidx.compose.ui.Size(rectWidth, rectHeight),
                style = Stroke(width = 4.dp.toPx())
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Button(onClick = onSettingsClick) {
                    Text("Configurar mm")
                }
                
                FloatingActionButton(onClick = {
                    val photoFile = File(context.filesDir, "photo_${System.currentTimeMillis()}.jpg")
                    imageCapture.takePicture(photoFile, executor, object : ImageCapture.OnImageSavedCallback {
                        override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                            onImageCaptured(photoFile.absolutePath)
                        }
                        override fun onError(exc: ImageCaptureException) {
                            exc.printStackTrace()
                        }
                    })
                }) {
                    Text("CAPTURAR")
                }
            }
        }
    }
}
