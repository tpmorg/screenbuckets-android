package com.screenbuckets.service.analysis

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.remote.AnalysisRequest
import com.screenbuckets.data.remote.LLMService
import com.screenbuckets.data.remote.Message
import com.screenbuckets.data.repository.ScreenshotRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await
import java.io.File
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@HiltWorker
class ScreenshotAnalysisWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val screenshotRepository: ScreenshotRepository,
    private val llmService: LLMService
) : CoroutineWorker(appContext, workerParams) {
    
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    
    override suspend fun doWork(): Result {
        val screenshotId = inputData.getLong(KEY_SCREENSHOT_ID, -1)
        if (screenshotId == -1L) {
            return Result.failure()
        }
        
        return try {
            // Get the screenshot from the database
            val screenshot = screenshotRepository.getScreenshotById(screenshotId) ?: return Result.failure()
            
            // Process the screenshot
            val updatedScreenshot = processScreenshot(screenshot)
            
            // Update the database
            screenshotRepository.updateScreenshot(updatedScreenshot)
            
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Error processing screenshot: ${e.message}")
            Result.retry()
        }
    }
    
    private suspend fun processScreenshot(screenshot: Screenshot): Screenshot {
        // Load the bitmap
        val file = File(screenshot.filePath)
        if (!file.exists()) {
            throw IllegalStateException("Screenshot file does not exist: ${screenshot.filePath}")
        }
        
        val bitmap = BitmapFactory.decodeFile(file.absolutePath)
        
        // Extract text with OCR
        val extractedText = extractText(bitmap)
        
        // Get embeddings from LLM
        val embedding = getEmbeddings(extractedText)
        
        // Get analysis from LLM
        val analysis = analyzeScreenshot(screenshot, extractedText)
        
        // Update screenshot with processed data
        return screenshot.copy(
            extractedText = extractedText,
            embedding = embedding,
            categories = analysis.categories,
            tags = analysis.tags,
            description = analysis.description,
            isProcessed = true
        )
    }
    
    private suspend fun extractText(bitmap: Bitmap): String {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        return try {
            val result = textRecognizer.process(image).await()
            result.text
        } catch (e: Exception) {
            Log.e(TAG, "Error in text recognition: ${e.message}")
            ""
        }
    }
    
    private suspend fun getEmbeddings(text: String): List<Float> {
        if (text.isBlank()) return emptyList()
        
        return try {
            val response = llmService.getEmbeddings(
                "Bearer $API_KEY", // In a real app, get this from secure storage
                com.screenbuckets.data.remote.EmbeddingRequest(input = text)
            )
            
            if (response.data.isNotEmpty()) {
                response.data[0].embedding
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting embeddings: ${e.message}")
            emptyList()
        }
    }
    
    private suspend fun analyzeScreenshot(
        screenshot: Screenshot,
        extractedText: String
    ): AnalysisResult {
        if (extractedText.isBlank()) {
            return AnalysisResult(emptyList(), emptyList(), "Screenshot without text content")
        }
        
        return try {
            val prompt = """
                Analyze this screenshot from the app "${screenshot.appName}". 
                The extracted text is: "${extractedText}".
                
                1. Provide 1-3 relevant categories for this screenshot
                2. Generate 3-5 relevant tags for this screenshot
                3. Write a brief one-sentence description of what this screenshot contains
                
                Format your response as valid JSON with the following structure:
                {
                  "categories": ["category1", "category2", ...],
                  "tags": ["tag1", "tag2", ...],
                  "description": "A brief description"
                }
            """.trimIndent()
            
            val messages = listOf(Message("user", prompt))
            
            val response = llmService.analyzeScreenshot(
                "Bearer $API_KEY", // In a real app, get this from secure storage
                AnalysisRequest(messages = messages)
            )
            
            if (response.choices.isNotEmpty()) {
                // Parse the JSON response
                val content = response.choices[0].message.content
                val json = kotlin.runCatching { 
                    android.util.JsonReader(java.io.StringReader(content))
                }.getOrNull()
                
                if (json != null) {
                    // For simplicity, we're using a placeholder parsing method here
                    // In a real app, you'd want to use a proper JSON parser
                    // This is just to show the structure
                    
                    AnalysisResult(
                        categories = listOf("App UI", "Information"),
                        tags = listOf("screenshot", screenshot.appName.lowercase(), "mobile"),
                        description = "Screenshot from ${screenshot.appName} showing interface elements"
                    )
                } else {
                    AnalysisResult(
                        categories = listOf("Uncategorized"),
                        tags = listOf("screenshot", screenshot.appName.lowercase()),
                        description = "Screenshot from ${screenshot.appName}"
                    )
                }
            } else {
                AnalysisResult(
                    categories = listOf("Uncategorized"),
                    tags = listOf("screenshot", screenshot.appName.lowercase()),
                    description = "Screenshot from ${screenshot.appName}"
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error analyzing screenshot: ${e.message}")
            AnalysisResult(
                categories = listOf("Uncategorized"),
                tags = listOf("screenshot"),
                description = "Screenshot from ${screenshot.appName}"
            )
        }
    }
    
    data class AnalysisResult(
        val categories: List<String>,
        val tags: List<String>,
        val description: String
    )
    
    companion object {
        private const val TAG = "ScreenshotAnalysisWorker"
        const val KEY_SCREENSHOT_ID = "key_screenshot_id"
        
        // In a real app, this would be stored securely and not hard-coded
        private const val API_KEY = "your_api_key_here"
    }
}