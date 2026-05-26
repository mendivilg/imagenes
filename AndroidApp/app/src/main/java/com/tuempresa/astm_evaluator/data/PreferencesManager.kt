package com.tuempresa.astm_evaluator.data

import android.content.Context
import android.content.SharedPreferences

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("astm_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_WIDTH = "panel_width_mm"
        private const val KEY_HEIGHT = "panel_height_mm"
        private const val KEY_ERROR_COUNT = "error_count"
        private const val KEY_LAST_MODEL_VERSION = "last_model_version"
        private const val DEFAULT_WIDTH = 70f
        private const val DEFAULT_HEIGHT = 150f
        const val RETRAIN_THRESHOLD = 50
    }

    var panelWidth: Float
        get() = prefs.getFloat(KEY_WIDTH, DEFAULT_WIDTH)
        set(value) = prefs.edit().putFloat(KEY_WIDTH, value).apply()

    var panelHeight: Float
        get() = prefs.getFloat(KEY_HEIGHT, DEFAULT_HEIGHT)
        set(value) = prefs.edit().putFloat(KEY_HEIGHT, value).apply()

    var errorCount: Int
        get() = prefs.getInt(KEY_ERROR_COUNT, 0)
        set(value) = prefs.edit().putInt(KEY_ERROR_COUNT, value).apply()

    var lastModelVersion: String
        get() = prefs.getString(KEY_LAST_MODEL_VERSION, "v0") ?: "v0"
        set(value) = prefs.edit().putString(KEY_LAST_MODEL_VERSION, value).apply()

    fun incrementErrorCount(): Boolean {
        val current = errorCount + 1
        errorCount = current
        return current >= RETRAIN_THRESHOLD
    }

    fun resetErrorCount() {
        errorCount = 0
    }
    
    fun getPanelDimensions(): Pair<Float, Float> {
        return Pair(panelWidth, panelHeight)
    }
}
