package com.wdmaster.app.data.local

import androidx.room.*
import com.wdmaster.app.data.model.Router
import kotlinx.coroutines.flow.Flow

@Dao
interface RouterDao {
    
    @Query("SELECT * FROM routers WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getActiveRouters(): Flow<List<Router>>
    
    @Query("SELECT * FROM routers ORDER BY createdAt DESC")
    fun getAllRouters(): Flow<List<Router>>
    
    @Query("SELECT * FROM routers WHERE id = :id")
    suspend fun getRouterById(id: Int): Router?
    
    @Query("SELECT * FROM routers WHERE id = :id")
    fun getRouterByIdFlow(id: Int): Flow<Router?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRouter(router: Router): Long
    
    @Update
    suspend fun updateRouter(router: Router)
    
    @Delete
    suspend fun deleteRouter(router: Router)
    
    @Query("UPDATE routers SET isActive = :active WHERE id = :id")
    suspend fun toggleActive(id: Int, active: Boolean)
    
    @Query("SELECT COUNT(*) FROM routers")
    suspend fun getRouterCount(): Int
    
    @Query("SELECT * FROM routers WHERE macPrefix LIKE :prefix%")
    fun getRoutersByPrefix(prefix: String): Flow<List<Router>>
}