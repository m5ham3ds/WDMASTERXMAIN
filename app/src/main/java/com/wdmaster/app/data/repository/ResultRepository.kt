package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.ResultDao
import com.wdmaster.app.data.model.TestResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ResultRepository @Inject constructor(
    private val resultDao: ResultDao
) {
    fun getRecentResults(limit: Int = 100): Flow<List<TestResult>> = 
        resultDao.getRecentResults(limit)
    
    fun getSuccessfulResults(): Flow<List<TestResult>> = 
        resultDao.getSuccessfulResults()
    
    fun getFailedResults(): Flow<List<TestResult>> = 
        resultDao.getFailedResults()
    
    suspend fun getResultById(id: Long): TestResult? = 
        resultDao.getResultById(id)
    
    suspend fun getResultByCard(card: String): TestResult? = 
        resultDao.getResultByCard(card)
    
    suspend fun addResult(result: TestResult): Long = 
        resultDao.insertResult(result)
    
    suspend fun addResults(results: List<TestResult>) = 
        resultDao.insertResults(results)
    
    suspend fun deleteResult(result: TestResult) = 
        resultDao.deleteResult(result)
    
    suspend fun deleteOldResults() {
        val threshold = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L)
        resultDao.deleteOldResults(threshold)
    }
    
    suspend fun getResultCount(): Int = resultDao.getResultCount()
    
    suspend fun getSuccessCount(): Int = resultDao.getSuccessCount()
    
    suspend fun getFailureCount(): Int = resultDao.getFailureCount()
    
    suspend fun clearAllResults() = resultDao.clearAllResults()
    
    fun getResultsByDateRange(start: Long, end: Long): Flow<List<TestResult>> = 
        resultDao.getResultsByDateRange(start, end)
}