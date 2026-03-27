package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.model.CardConfig
import com.wdmaster.app.domain.learning.PatternLearningSystem
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GenerateCardUseCase @Inject constructor(
    private val patternLearningSystem: PatternLearningSystem
) {

    operator fun invoke(
        config: CardConfig,
        useLearnedPatterns: Boolean = true,
        routerId: Int? = null
    ): String {
        return if (useLearnedPatterns && routerId != null) {
            generateWithLearning(config, routerId)
        } else {
            generateRandom(config)
        }
    }

    private fun generateWithLearning(config: CardConfig, routerId: Int?): String {

        val patterns = runBlocking {
            patternLearningSystem.getBestPatterns(routerId).first()
        }

        return if (patterns.isNotEmpty()) {
            val bestPattern = patterns.maxByOrNull { it.getWeight() }
            bestPattern?.toCard(config.allowedChars) ?: generateRandom(config)
        } else {
            generateRandom(config)
        }
    }

    private fun generateRandom(config: CardConfig): String {
        val sb = StringBuilder(config.prefix)
        val remainingLength = config.length - config.prefix.length

        repeat(remainingLength) {
            val randomIndex = (Math.random() * config.allowedChars.length).toInt()
            sb.append(config.allowedChars[randomIndex])
        }

        return sb.toString()
    }

    fun validateConfig(config: CardConfig): Boolean {
        return config.length > 0 &&
                config.length <= 64 &&
                config.maxTries > 0 &&
                config.allowedChars.isNotEmpty() &&
                config.prefix.length < config.length
    }

    fun generateBatch(config: CardConfig, count: Int, routerId: Int? = null): List<String> {
        return List(count) {
            invoke(config, routerId = routerId)
        }
    }
}
