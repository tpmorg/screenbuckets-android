package com.screenbuckets.utils

import com.screenbuckets.data.model.Screenshot
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Utility class for vector operations used in screenshot similarity search.
 */
object VectorUtil {
    
    /**
     * Calculate cosine similarity between two embedding vectors.
     * Returns a value between -1 and 1, where 1 means identical vectors.
     */
    fun cosineSimilarity(v1: List<Float>, v2: List<Float>): Float {
        if (v1.isEmpty() || v2.isEmpty() || v1.size != v2.size) {
            return 0f
        }
        
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f
        
        for (i in v1.indices) {
            dotProduct += v1[i] * v2[i]
            norm1 += v1[i].pow(2)
            norm2 += v2[i].pow(2)
        }
        
        // Avoid division by zero
        if (norm1 == 0f || norm2 == 0f) {
            return 0f
        }
        
        return dotProduct / (sqrt(norm1) * sqrt(norm2))
    }
    
    /**
     * Find the most similar screenshots to the given embedding vector.
     * Orders results by cosine similarity.
     */
    fun findSimilarScreenshots(
        queryEmbedding: List<Float>,
        screenshots: List<Screenshot>,
        limit: Int = 10
    ): List<Screenshot> {
        // Filter out screenshots without embeddings
        val validScreenshots = screenshots.filter { it.embedding != null && it.embedding.isNotEmpty() }
        
        // Calculate similarity scores for each screenshot
        val scoredScreenshots = validScreenshots.map { screenshot ->
            val similarity = cosineSimilarity(queryEmbedding, screenshot.embedding!!)
            Pair(screenshot, similarity)
        }
        
        // Sort by similarity score (highest first) and take top results
        return scoredScreenshots
            .sortedByDescending { it.second }
            .take(limit)
            .map { it.first }
    }
}