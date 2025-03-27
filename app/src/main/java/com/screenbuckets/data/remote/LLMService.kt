package com.screenbuckets.data.remote

import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface LLMService {
    
    @POST("v1/embeddings")
    suspend fun getEmbeddings(
        @Header("Authorization") apiKey: String,
        @Body request: EmbeddingRequest
    ): EmbeddingResponse
    
    @POST("v1/chat/completions")
    suspend fun analyzeScreenshot(
        @Header("Authorization") apiKey: String,
        @Body request: AnalysisRequest
    ): AnalysisResponse
}

data class EmbeddingRequest(
    val model: String = "text-embedding-3-small",
    val input: String
)

data class EmbeddingResponse(
    val data: List<Embedding>,
    val model: String,
    val usage: Usage
)

data class Embedding(
    val embedding: List<Float>,
    val index: Int
)

data class AnalysisRequest(
    val model: String = "gpt-4o",
    val messages: List<Message>,
    val temperature: Double = 0.7
)

data class Message(
    val role: String,
    val content: String
)

data class AnalysisResponse(
    val id: String,
    val choices: List<Choice>,
    val usage: Usage
)

data class Choice(
    val message: Message,
    val finish_reason: String,
    val index: Int
)

data class Usage(
    val prompt_tokens: Int,
    val completion_tokens: Int? = null,
    val total_tokens: Int
)