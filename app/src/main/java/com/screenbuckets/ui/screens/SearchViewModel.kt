package com.screenbuckets.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.remote.EmbeddingRequest
import com.screenbuckets.data.remote.LLMService
import com.screenbuckets.data.repository.ScreenshotRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val screenshotRepository: ScreenshotRepository,
    private val llmService: LLMService
) : ViewModel() {
    
    private val _searchResults = MutableStateFlow<List<Screenshot>>(emptyList())
    val searchResults: StateFlow<List<Screenshot>> = _searchResults.asStateFlow()
    
    val searchFlow = MutableSharedFlow<String>()
    
    // In a real app, this would be stored securely and not hard-coded
    private val apiKey = "your_api_key_here"
    
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
                // First try text-based search
                val textResults = screenshotRepository.searchScreenshotsByText(query)
                
                // If we get results, update the UI
                textResults.collect { results ->
                    if (results.isNotEmpty()) {
                        _searchResults.value = results
                    } else {
                        // If no text results, try semantic search with embeddings
                        semanticSearch(query)
                    }
                }
            } catch (e: Exception) {
                Timber.e(e, "Error searching screenshots")
            }
        }
    }
    
    private suspend fun semanticSearch(query: String) {
        try {
            // Get embedding for the search query
            val response = llmService.getEmbeddings(
                "Bearer $apiKey",
                EmbeddingRequest(input = query)
            )
            
            if (response.data.isNotEmpty()) {
                val embedding = response.data[0].embedding
                
                // Search for similar screenshots
                val results = screenshotRepository.searchByVectorSimilarity(embedding)
                _searchResults.value = results
            }
        } catch (e: Exception) {
            Timber.e(e, "Error performing semantic search")
        }
    }
}