package com.screenbuckets.ui.screens

import androidx.lifecycle.ViewModel
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.repository.ScreenshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val screenshotRepository: ScreenshotRepository
) : ViewModel() {
    
    val screenshots: Flow<List<Screenshot>> = screenshotRepository.getAllScreenshots()
}