package com.wdmaster.app.domain.learning

import com.wdmaster.app.data.local.PatternDao
import com.wdmaster.app.data.model.CardPattern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternLearningSystem @Inject constructor(
    private val patternDao: PatternDao
) {

    fun extractPattern(card: String, routerId: Int?): String {
        val parts = card.split(":")
        return if (parts.size >= 3) {
            "${parts[0]}:${parts[1]}:${parts[2]}:*:*:*"
        } else if (card.length >= 6) {
            "${card.take(6)}:*"
        } else {
            "$card:*:*"
        }
    }

    suspend fun recordSuccess(card: String, routerId: Int?) {
        val pattern = extractPattern(card, routerId)

        // ✅ الحل هنا
        val patterns = patternDao.getPatternsForRouter(routerId).first()

        val existing = patterns.firstOrNull { it.pattern == pattern }

        if (existing != null) {
            patternDao.incrementSuccess(existing.id)
        } else {
            patternDao.savePattern(
                CardPattern(
                    pattern = pattern,
                    routerId = routerId,
                    successCount = 1,
                    lastUsed = System.currentTimeMillis()
                )
            )
        }
    }

    fun getBestPatterns(routerId: Int?): Flow<List<CardPattern>> {
        return patternDao.getPatternsForRouter(routerId)
    }

    fun generateWithPattern(basePattern: String, allowedChars: String): String {
        return basePattern.map { char ->
            if (char == '*' || char == 'X') {
                allowedChars.random()
            } else {
                char
            }
        }.joinToString("")
    }

    suspend fun cleanup() {
        val threshold = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        patternDao.cleanupOldPatterns(threshold)
    }

    suspend fun getPatternWeight(pattern: String): Double {
        val cardPattern = patternDao.getPatternByString(pattern)
        return cardPattern?.getWeight() ?: 0.0
    }
}
