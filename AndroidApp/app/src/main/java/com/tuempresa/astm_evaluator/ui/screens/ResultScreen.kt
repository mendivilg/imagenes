package com.tuempresa.astm_evaluator.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.tuempresa.astm_evaluator.data.PreferencesManager
import com.tuempresa.astm_evaluator.model.*
import java.io.File

@Composable
fun ResultScreen(
    imagePath: String,
    initialPrediction: AstmClassifier.PredictionResult,
    prefsManager: PreferencesManager,
    onSaveResult: (InspectionResult) -> Unit,
    onRetake: () -> Unit
) {
    var selectedType by remember { mutableStateOf(initialPrediction.type) }
    
    var rustGrade by remember { mutableIntStateOf(if (initialPrediction.type == DefectType.RUST) initialPrediction.gradeOrSize else 10) }
    var rustDist by remember { mutableStateOf(if (initialPrediction.type == DefectType.RUST) initialPrediction.distributionOrFrequency else "S") }
    
    var blisterSize by remember { mutableIntStateOf(if (initialPrediction.type == DefectType.BLISTER) initialPrediction.gradeOrSize else 10) }
    var blisterFreq by remember { mutableStateOf(if (initialPrediction.type == DefectType.BLISTER) initialPrediction.distributionOrFrequency else "F") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Resultado de Inspección", style = MaterialTheme.typography.headlineSmall)
        
        Text("Imagen: ${File(imagePath).name}", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.padding(vertical = 8.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Tipo de Defecto", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilterChip(
                        selected = selectedType == DefectType.RUST,
                        onClick = { selectedType = DefectType.RUST },
                        label = { Text("Óxido (D610)") }
                    )
                    FilterChip(
                        selected = selectedType == DefectType.BLISTER,
                        onClick = { selectedType = DefectType.BLISTER },
                        label = { Text("Ampollas (D714)") }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (selectedType == DefectType.RUST) {
                    Text("Grado de Óxido (0-10)", style = MaterialTheme.typography.titleSmall)
                    Slider(
                        value = rustGrade.toFloat(),
                        onValueChange = { rustGrade = it.toInt() },
                        valueRange = 0f..10f,
                        steps = 9
                    )
                    Text("Valor: $rustGrade (${getRustPercentage(rustGrade)}% área)", style = MaterialTheme.typography.bodySmall)

                    Text("Distribución", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("S", "G", "P", "H").forEach { dist ->
                            AssistChip(
                                onClick = { rustDist = dist },
                                label = { Text(dist) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (rustDist == dist) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                } else {
                    Text("Tamaño de Ampolla", style = MaterialTheme.typography.titleSmall)
                    val sizes = listOf(10, 8, 6, 4, 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        sizes.forEach { size ->
                            AssistChip(
                                onClick = { blisterSize = size },
                                label = { Text("#$size") },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (blisterSize == size) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }

                    Text("Frecuencia", style = MaterialTheme.typography.titleSmall)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("F", "M", "MD", "D").forEach { freq ->
                            AssistChip(
                                onClick = { blisterFreq = freq },
                                label = { Text(freq) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = if (blisterFreq == freq) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                                )
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            OutlinedButton(onClick = onRetake) {
                Text("Rechazar / Repetir")
            }
            
            Button(
                onClick = {
                    val result = if (selectedType == DefectType.RUST) {
                        InspectionResult(
                            imagePath = imagePath,
                            rustEval = RustEvaluation(rustGrade, RustDistribution.values().find { it.label == rustDist }!!, 0.0),
                            panelWidthMm = prefsManager.panelWidth,
                            panelHeightMm = prefsManager.panelHeight,
                            needsRetraining = (initialPrediction.confidence < 0.8 || initialPrediction.type != selectedType)
                        )
                    } else {
                        InspectionResult(
                            imagePath = imagePath,
                            blisterEval = BlisterEvaluation(blisterSize, BlisterFrequency.values().find { it.label == blisterFreq }!!),
                            panelWidthMm = prefsManager.panelWidth,
                            panelHeightMm = prefsManager.panelHeight,
                            needsRetraining = (initialPrediction.confidence < 0.8 || initialPrediction.type != selectedType)
                        )
                    }
                    onSaveResult(result)
                },
                enabled = true
            ) {
                Text("Guardar")
            }
        }
    }
}

fun getRustPercentage(grade: Int): String {
    return when (grade) {
        10 -> "< 0.01"
        9 -> "0.01 - 0.03"
        8 -> "0.03 - 0.1"
        7 -> "0.1 - 0.3"
        6 -> "0.3 - 1.0"
        5 -> "1.0 - 3.0"
        4 -> "3.0 - 10.0"
        3 -> "10.0 - 16.0"
        2 -> "16.0 - 33.0"
        1 -> "33.0 - 50.0"
        0 -> "> 50.0"
        else -> "Desconocido"
    }
}
