package com.screenbuckets.ui.screens

import androidx.lifecycle.ViewModel
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.repository.ScreenshotRepository
import kotlinx.coroutines.flow.Flow

class HomeViewModel : ViewModel() {
    
    private val repository = ScreenshotRepository.getInstance()
    
    val screenshots: Flow<List<Screenshot>> = repository.getAllScreenshots()
}