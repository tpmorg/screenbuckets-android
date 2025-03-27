package com.screenbuckets.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import com.screenbuckets.data.local.ScreenshotDao
import com.screenbuckets.data.model.Screenshot
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.io.FileOutputStream
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class ScreenshotRepository @Inject constructor(
    private val context: Context,
    private val screenshotDao: ScreenshotDao
) {
    
    private val screenshotsDir: File by lazy {
        File(context.filesDir, "screenshots").apply {
            if (!exists()) {
                mkdirs()
            }
        }
    }
    
    suspend fun saveScreenshot(
        bitmap: Bitmap,
        appName: String,
        appPackage: String
    ): Screenshot {
        // Save bitmap to file
        val timestamp = LocalDateTime.now()
        val fileName = "screenshot_${timestamp.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"))}.jpg"
        val file = File(screenshotsDir, fileName)
        
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
        }
        
        // Create screenshot entry
        val screenshot = Screenshot(
            filePath = file.absolutePath,
            timestamp = timestamp,
            appName = appName,
            appPackage = appPackage,
            isProcessed = false
        )
        
        // Save to database
        val id = screenshotDao.insertScreenshot(screenshot)
        return screenshot.copy(id = id)
    }
    
    suspend fun updateScreenshot(screenshot: Screenshot) {
        screenshotDao.updateScreenshot(screenshot)
    }
    
    suspend fun deleteScreenshot(screenshot: Screenshot) {
        // Delete the file
        File(screenshot.filePath).delete()
        // Delete from database
        screenshotDao.deleteScreenshot(screenshot)
    }
    
    fun getAllScreenshots(): Flow<List<Screenshot>> {
        return screenshotDao.getAllScreenshots()
    }
    
    suspend fun getUnprocessedScreenshot(): Screenshot? {
        return screenshotDao.getUnprocessedScreenshot()
    }
    
    fun getScreenshotsByApp(packageName: String): Flow<List<Screenshot>> {
        return screenshotDao.getScreenshotsByApp(packageName)
    }
    
    fun getAllApps(): Flow<List<String>> {
        return screenshotDao.getAllApps()
    }
    
    fun searchScreenshotsByText(query: String): Flow<List<Screenshot>> {
        return screenshotDao.searchScreenshotsByText(query)
    }
    
    suspend fun searchByVectorSimilarity(embedding: List<Float>, limit: Int = 10): List<Screenshot> {
        val embeddingString = embedding.joinToString(",")
        val query = """
            SELECT s.* FROM screenshots s
            JOIN screenshot_vectors v ON s.id = v.rowid
            ORDER BY vec_cosine_distance(v.embedding, vec_to_array('$embeddingString'))
            LIMIT $limit
        """.trimIndent()
        
        return screenshotDao.vectorSearch(SimpleSQLiteQuery(query))
    }
    
    suspend fun getScreenshotById(id: Long): Screenshot? {
        return screenshotDao.getScreenshotById(id)
    }
}