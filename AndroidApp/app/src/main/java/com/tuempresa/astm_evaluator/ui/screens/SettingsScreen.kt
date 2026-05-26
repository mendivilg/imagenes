package com.tuempresa.astm_evaluator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tuempresa.astm_evaluator.data.PreferencesManager

@Composable
fun SettingsScreen(
    prefsManager: PreferencesManager,
    onSave: () -> Unit,
    onBack: () -> Unit
) {
    var width by remember { mutableStateOf(prefsManager.panelWidth.toString()) }
    var height by remember { mutableStateOf(prefsManager.panelHeight.toString()) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configuración del Panel", style = MaterialTheme.typography.headlineMedium)
        
        OutlinedTextField(
            value = width,
            onValueChange = { width = it },
            label = { Text("Ancho (mm)") },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )

        OutlinedTextField(
            value = height,
            onValueChange = { height = it },
            label = { Text("Alto (mm)") },
            singleLine = true,
            keyboardOptions = androidx.compose.ui.text.input.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number)
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { 
                prefsManager.panelWidth = width.toFloatOrNull() ?: 70f
                prefsManager.panelHeight = height.toFloatOrNull() ?: 150f
                onSave()
            }, modifier = Modifier.weight(1f)) {
                Text("Guardar")
            }
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) {
                Text("Cancelar")
            }
        }
        
        Text("Estos valores se usarán para calcular el área real y superponer el marco guía.", style = MaterialTheme.typography.bodySmall)
    }
}
