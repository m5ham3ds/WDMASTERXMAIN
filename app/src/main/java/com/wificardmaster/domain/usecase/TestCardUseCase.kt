package com.wdmaster.app.domain.usecase

import com.wdmaster.app.data.model.TestResult
import com.wdmaster.app.data.repository.ResultRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TestCardUseCase @Inject constructor(
    private val resultRepository: ResultRepository
) {

    suspend operator fun invoke(
        card: String,
        routerId: Int? = null,
        timeout: Long = 5000L
    ): TestResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        
        val success = try {
            performTest(card, timeout)
        } catch (e: Exception) {
            false
        }
        
        val responseTime = System.currentTimeMillis() - startTime
        
        TestResult(
            card = card,
            success = success,
            routerId = routerId,
            responseTime = responseTime,
            errorCode = if (!success) "TEST_FAILED" else null
        )
    }

    private suspend fun performTest(card: String, timeout: Long): Boolean {
        // TODO: Implement real WiFi/Bluetooth testing logic
        // This is a placeholder for actual hardware testing
        
        return withContext(Dispatchers.IO) {
            // Simulate network/hardware delay
            kotlinx.coroutines.delay(50 + (Math.random() * 100).toLong())
            
            // Placeholder - replace with actual test implementation
            Math.random() > 0.95
        }
    }

    suspend fun testBatch(
        cards: List<String>,
        routerId: Int? = null,
        onProgress: (Int, Int) -> Unit
    ): List<TestResult> = withContext(Dispatchers.IO) {
        val results = mutableListOf<TestResult>()
        
        cards.forEachIndexed { index, card ->
            val result = invoke(card, routerId)
            results.add(result)
            onProgress(index + 1, cards.size)
        }
        
        results
    }

    suspend fun saveResult(result: TestResult) {
        resultRepository.addResult(result)
    }

    suspend fun saveResults(results: List<TestResult>) {
        resultRepository.addResults(results)
    }
}