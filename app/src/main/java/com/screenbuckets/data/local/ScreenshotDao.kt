package com.screenbuckets.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.Update
import androidx.sqlite.db.SupportSQLiteQuery
import com.screenbuckets.data.model.Screenshot
import kotlinx.coroutines.flow.Flow

@Dao
interface ScreenshotDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScreenshot(screenshot: Screenshot): Long
    
    @Update
    suspend fun updateScreenshot(screenshot: Screenshot)
    
    @Delete
    suspend fun deleteScreenshot(screenshot: Screenshot)
    
    @Query("SELECT * FROM screenshots ORDER BY timestamp DESC")
    fun getAllScreenshots(): Flow<List<Screenshot>>
    
    @Query("SELECT * FROM screenshots WHERE id = :id")
    suspend fun getScreenshotById(id: Long): Screenshot?
    
    @Query("SELECT * FROM screenshots WHERE isProcessed = 0 LIMIT 1")
    suspend fun getUnprocessedScreenshot(): Screenshot?
    
    @Query("SELECT * FROM screenshots WHERE appPackage = :packageName ORDER BY timestamp DESC")
    fun getScreenshotsByApp(packageName: String): Flow<List<Screenshot>>
    
    @Query("SELECT DISTINCT appName FROM screenshots ORDER BY appName")
    fun getAllApps(): Flow<List<String>>
    
    @Query("SELECT * FROM screenshots WHERE extractedText LIKE '%' || :query || '%' ORDER BY timestamp DESC")
    fun searchScreenshotsByText(query: String): Flow<List<Screenshot>>
    
    @Query("SELECT * FROM screenshots WHERE embedding IS NOT NULL")
    suspend fun getAllScreenshotsWithEmbeddings(): List<Screenshot>
}