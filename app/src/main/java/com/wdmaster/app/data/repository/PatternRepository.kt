package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.PatternDao
import com.wdmaster.app.data.model.CardPattern
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PatternRepository @Inject constructor(
    private val patternDao: PatternDao
) {
    val topPatternsFlow: Flow<List<CardPattern>> = patternDao.getTopPatterns()
    
    fun getPatternsForRouter(routerId: Int?): Flow<List<CardPattern>> = 
        patternDao.getPatternsForRouter(routerId)
    
    suspend fun getPatternByString(pattern: String): CardPattern? = 
        patternDao.getPatternByString(pattern)
    
    suspend fun savePattern(pattern: CardPattern): Long = 
        patternDao.savePattern(pattern)
    
    suspend fun updatePattern(pattern: CardPattern) = 
        patternDao.updatePattern(pattern)
    
    suspend fun incrementSuccess(patternId: Int) = 
        patternDao.incrementSuccess(patternId)
    
    suspend fun deletePattern(pattern: CardPattern) = 
        patternDao.deletePattern(pattern)
    
    suspend fun cleanupOldPatterns() {
        val threshold = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)
        patternDao.cleanupOldPatterns(threshold)
    }
    
    suspend fun getPatternCount(): Int = patternDao.getPatternCount()
    
    suspend fun clearAllPatterns() = patternDao.clearAllPatterns()
}