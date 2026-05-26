package com.tuempresa.astm_evaluator.data

import android.content.Context
import java.io.File

class DriveManager(private val context: Context) {
    
    suspend fun uploadFile(file: File, folderPath: String): Boolean {
        return try {
            println("Simulando subida de ${file.name} a $folderPath")
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
