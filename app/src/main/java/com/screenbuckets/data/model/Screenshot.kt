package com.screenbuckets.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDateTime
import java.time.ZoneOffset

@Entity(tableName = "screenshots")
@TypeConverters(ScreenshotConverters::class)
data class Screenshot(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val filePath: String,
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val appName: String,
    val appPackage: String,
    val extractedText: String? = null,
    val embedding: List<Float>? = null,
    val categories: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val description: String? = null,
    val isProcessed: Boolean = false
)

class ScreenshotConverters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromTimestamp(timestamp: Long?): LocalDateTime? {
        return timestamp?.let { LocalDateTime.ofEpochSecond(it, 0, ZoneOffset.UTC) }
    }
    
    @TypeConverter
    fun toTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.toEpochSecond(ZoneOffset.UTC)
    }
    
    @TypeConverter
    fun fromStringList(value: String?): List<String> {
        if (value == null) {
            return emptyList()
        }
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun toStringList(list: List<String>?): String {
        return gson.toJson(list ?: emptyList<String>())
    }
    
    @TypeConverter
    fun fromFloatList(value: String?): List<Float>? {
        if (value == null) {
            return null
        }
        val listType = object : TypeToken<List<Float>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun toFloatList(list: List<Float>?): String? {
        return if (list == null) null else gson.toJson(list)
    }
}