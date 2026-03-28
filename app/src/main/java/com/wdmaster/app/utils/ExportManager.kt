package com.wdmaster.app.utils

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.wdmaster.app.data.model.TestResult
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExportManager @Inject constructor(
    private val context: Context
) {

    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())

    suspend fun exportToCSV(
        results: List<TestResult>,
        fileName: String? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            val name = fileName ?: "wdmaster_export_${dateFormat.format(Date())}.csv"
            val file = File(context.getExternalFilesDir("exports"), name)
            
            FileWriter(file).use { writer ->
                // Write header
                writer.append("ID,Card,Success,Timestamp,RouterID,ResponseTime,ErrorCode\n")
                
                // Write data
                results.forEach { result ->
                    writer.append("${result.id},")
                    writer.append("${result.card},")
                    writer.append("${result.success},")
                    writer.append("${result.timestamp},")
                    writer.append("${result.routerId ?: ""},")
                    writer.append("${result.responseTime},")
                    writer.append("${result.errorCode ?: ""}")
                    writer.append("\n")
                }
            }
                        file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToJSON(
        results: List<TestResult>,
        fileName: String? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            val name = fileName ?: "wdmaster_export_${dateFormat.format(Date())}.json"
            val file = File(context.getExternalFilesDir("exports"), name)
            
            val exportData = mapOf(
                "export_date" to System.currentTimeMillis(),
                "total_results" to results.size,
                "success_count" to results.count { it.success },
                "failed_count" to results.count { !it.success },
                "results" to results
            )
            
            FileWriter(file).use { writer ->
                writer.append(gson.toJson(exportData))
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun exportToTXT(
        results: List<TestResult>,
        fileName: String? = null
    ): File? = withContext(Dispatchers.IO) {
        try {
            val name = fileName ?: "wdmaster_export_${dateFormat.format(Date())}.txt"
            val file = File(context.getExternalFilesDir("exports"), name)
            
            FileWriter(file).use { writer ->
                writer.append("WiFi Card Master Pro - Export Report\n")
                writer.append("Generated: ${dateFormat.format(Date())}\n")
                writer.append("========================================\n\n")
                writer.append("Total: ${results.size}\n")
                writer.append("Success: ${results.count { it.success }}\n")
                writer.append("Failed: ${results.count { !it.success }}\n")
                writer.append("\n========================================\n\n")                
                results.forEach { result ->
                    writer.append("[${if (result.success) "✓" else "✗"}] ")
                    writer.append("${result.card} ")
                    writer.append("(${result.responseTime}ms)\n")
                }
            }
            
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun shareFile(file: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(Intent.createChooser(shareIntent, "Share Export File"))
    }

    suspend fun cleanupOldExports(daysToKeep: Int = 7) {
        withContext(Dispatchers.IO) {
            val exportDir = context.getExternalFilesDir("exports")
            val threshold = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L)
            
            exportDir?.listFiles()?.forEach { file ->
                if (file.lastModified() < threshold) {
                    file.delete()
                }
            }
        }
    }
}