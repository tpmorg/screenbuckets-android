package com.screenbuckets.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.repository.ScreenshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class ScreenshotDetailViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val screenshotRepository: ScreenshotRepository
) : ViewModel() {
    
    private val _screenshot = MutableStateFlow<Screenshot?>(null)
    val screenshot: StateFlow<Screenshot?> = _screenshot.asStateFlow()
    
    fun loadScreenshot(screenshotId: Long) {
        viewModelScope.launch {
            val result = screenshotRepository.getScreenshotById(screenshotId)
            _screenshot.value = result
        }
    }
    
    fun deleteScreenshot() {
        val currentScreenshot = _screenshot.value ?: return
        
        viewModelScope.launch {
            screenshotRepository.deleteScreenshot(currentScreenshot)
            _screenshot.value = null
        }
    }
    
    fun shareScreenshot() {
        val currentScreenshot = _screenshot.value ?: return
        
        val screenshotFile = File(currentScreenshot.filePath)
        if (!screenshotFile.exists()) return
        
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            screenshotFile
        )
        
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/jpeg"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            
            // Add description if available
            currentScreenshot.description?.let { description ->
                putExtra(Intent.EXTRA_TEXT, description)
            }
        }
        
        // Create the chooser intent
        val chooserIntent = Intent.createChooser(intent, "Share Screenshot")
        chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooserIntent)
    }
}