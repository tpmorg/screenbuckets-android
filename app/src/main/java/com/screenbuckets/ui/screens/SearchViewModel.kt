package com.screenbuckets.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.repository.ScreenshotRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

class SearchViewModel : ViewModel() {
    
    private val repository = ScreenshotRepository.getInstance()
    
    private val _searchResults = MutableStateFlow<List<Screenshot>>(emptyList())
    val searchResults: StateFlow<List<Screenshot>> = _searchResults.asStateFlow()
    
    val searchFlow = MutableSharedFlow<String>()
    
    @OptIn(FlowPreview::class)
    init {
        viewModelScope.launch {
            searchFlow
                .debounce(300L)
                .distinctUntilChanged()
                .collect { query ->
                    search(query)
                }
        }
    }
    
    private fun search(query: String) {
        if (query.isEmpty()) {
            _searchResults.value = emptyList()
            return
        }
        
        viewModelScope.launch {
            try {
                // For now, we'll just do text-based search
                // Later we can implement embedding search if needed
                repository.searchScreenshotsByText(query).collect { results ->
                    _searchResults.value = results
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching screenshots")
            }
        }
    }
}