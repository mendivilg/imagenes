package com.tuempresa.astm_evaluator.model

// Normas D610: Óxido
data class RustEvaluation(
    val grade: Int, // 0 a 10
    val distribution: RustDistribution, // S, G, P, H
    val percentage: Double = 0.0, // Calculado automáticamente según tabla ASTM
    val isManualAdjustment: Boolean = false
)

enum class RustDistribution(val label: String, val displayName: String) {
    SPOT("S", "Spot"),
    GENERAL("G", "General"),
    PINPOINT("P", "Pinpoint"),
    HYBRID("H", "Hybrid")
}

// Normas D714: Ampollamiento
data class BlisterEvaluation(
    val sizeNumber: Int, // 10, 8, 6, 4, 2
    val frequency: BlisterFrequency, // F, M, MD, D
    val isManualAdjustment: Boolean = false
)

enum class BlisterFrequency(val label: String, val displayName: String) {
    FEW("F", "Few"),
    MEDIUM("M", "Medium"),
    MEDIUM_DENSE("MD", "Medium Dense"),
    DENSE("D", "Dense")
}

// Resultado combinado de inspección
data class InspectionResult(
    val timestamp: Long = System.currentTimeMillis(),
    val imagePath: String,
    val rustEval: RustEvaluation? = null,
    val blisterEval: BlisterEvaluation? = null,
    val panelWidthMm: Float,
    val panelHeightMm: Float,
    val needsRetraining: Boolean = false, // Si el usuario corrigió al modelo
    val confidence: Float = 0.0f
)

// Tipo de defecto detectado
enum class DefectType {
    RUST,
    BLISTER,
    NONE
}

// Resultado de predicción del clasificador
data class PredictionResult(
    val type: DefectType,
    val gradeOrSize: Int, // 0-10 para óxido, 10/8/6/4/2 para ampollas
    val distributionOrFrequency: String, // S/G/P/H o F/M/MD/D
    val confidence: Float,
    val allProbabilities: Map<String, Float> = emptyMap() // Para depuración
)

// Helper para obtener porcentaje según ASTM D610
fun getRustPercentageRange(grade: Int): String {
    return when (grade) {
        10 -> "≤ 0.01"
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

// Helper para validar tamaño de ampolla según ASTM D714
fun isValidBlisterSize(size: Int): Boolean {
    return size in listOf(10, 8, 6, 4, 2)
}
