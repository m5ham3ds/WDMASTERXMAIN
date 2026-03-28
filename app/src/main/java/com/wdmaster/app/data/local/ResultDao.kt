package com.wdmaster.app.data.local

import androidx.room.*
import com.wdmaster.app.data.model.TestResult
import kotlinx.coroutines.flow.Flow

@Dao
interface ResultDao {
    
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentResults(limit: Int = 100): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results WHERE success = 1 ORDER BY timestamp DESC")
    fun getSuccessfulResults(): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results WHERE success = 0 ORDER BY timestamp DESC")
    fun getFailedResults(): Flow<List<TestResult>>
    
    @Query("SELECT * FROM test_results WHERE id = :id")
    suspend fun getResultById(id: Long): TestResult?
    
    @Query("SELECT * FROM test_results WHERE card = :card")
    suspend fun getResultByCard(card: String): TestResult?
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertResult(result: TestResult): Long
    
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertResults(results: List<TestResult>)
    
    @Delete
    suspend fun deleteResult(result: TestResult)
    
    @Query("DELETE FROM test_results WHERE timestamp < :threshold")
    suspend fun deleteOldResults(threshold: Long)
    
    @Query("SELECT COUNT(*) FROM test_results")
    suspend fun getResultCount(): Int
    
    @Query("SELECT COUNT(*) FROM test_results WHERE success = 1")
    suspend fun getSuccessCount(): Int
    
    @Query("SELECT COUNT(*) FROM test_results WHERE success = 0")
    suspend fun getFailureCount(): Int
    
    @Query("DELETE FROM test_results")
    suspend fun clearAllResults()
    
    @Query("SELECT * FROM test_results WHERE timestamp BETWEEN :start AND :end ORDER BY timestamp DESC")
    fun getResultsByDateRange(start: Long, end: Long): Flow<List<TestResult>>
}