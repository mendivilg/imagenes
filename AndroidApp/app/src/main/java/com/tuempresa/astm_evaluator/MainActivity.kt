package com.tuempresa.astm_evaluator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.tuempresa.astm_evaluator.ui.screens.CameraScreen
import com.tuempresa.astm_evaluator.ui.theme.AstmEvaluatorTheme
import com.tuempresa.astm_evaluator.data.PreferencesManager

class MainActivity : ComponentActivity() {
    private lateinit var prefsManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        prefsManager = PreferencesManager(this)

        setContent {
            AstmEvaluatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    CameraScreen(
                        prefsManager = prefsManager,
                        onImageCaptured = { path ->
                            println("Imagen capturada: $path")
                        },
                        onSettingsClick = {
                            println("Ir a configuración")
                        }
                    )
                }
            }
        }
    }
}
