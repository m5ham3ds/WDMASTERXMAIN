package com.wdmaster.app.data.local

import androidx.room.*
import com.wdmaster.app.data.model.CardPattern
import kotlinx.coroutines.flow.Flow

@Dao
interface PatternDao {
    
    @Query("SELECT * FROM successful_patterns ORDER BY successCount DESC LIMIT 20")
    fun getTopPatterns(): Flow<List<CardPattern>>
    
    @Query("SELECT * FROM successful_patterns WHERE routerId = :routerId OR routerId IS NULL ORDER BY successCount DESC")
    fun getPatternsForRouter(routerId: Int?): Flow<List<CardPattern>>
    
    @Query("SELECT * FROM successful_patterns WHERE pattern = :pattern")
    suspend fun getPatternByString(pattern: String): CardPattern?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePattern(pattern: CardPattern): Long
    
    @Update
    suspend fun updatePattern(pattern: CardPattern)
    
    @Query("UPDATE successful_patterns SET successCount = successCount + 1, lastUsed = :timestamp WHERE id = :patternId")
    suspend fun incrementSuccess(patternId: Int, timestamp: Long = System.currentTimeMillis())
    
    @Delete
    suspend fun deletePattern(pattern: CardPattern)
    
    @Query("DELETE FROM successful_patterns WHERE successCount = 0 AND lastUsed < :threshold")
    suspend fun cleanupOldPatterns(threshold: Long)
    
    @Query("SELECT COUNT(*) FROM successful_patterns")
    suspend fun getPatternCount(): Int
    
    @Query("DELETE FROM successful_patterns")
    suspend fun clearAllPatterns()
}