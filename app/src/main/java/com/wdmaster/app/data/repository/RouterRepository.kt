package com.wdmaster.app.data.repository

import com.wdmaster.app.data.local.RouterDao
import com.wdmaster.app.data.model.Router
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RouterRepository @Inject constructor(
    private val routerDao: RouterDao
) {
    val activeRoutersFlow: Flow<List<Router>> = routerDao.getActiveRouters()
    val allRoutersFlow: Flow<List<Router>> = routerDao.getAllRouters()
    
    suspend fun getRouterById(id: Int): Router? = routerDao.getRouterById(id)
    
    fun getRouterByIdFlow(id: Int): Flow<Router?> = routerDao.getRouterByIdFlow(id)
    
    suspend fun addRouter(router: Router): Long = routerDao.insertRouter(router)
    
    suspend fun updateRouter(router: Router) = routerDao.updateRouter(router)
    
    suspend fun deleteRouter(router: Router) = routerDao.deleteRouter(router)
    
    suspend fun toggleActive(id: Int, active: Boolean) = routerDao.toggleActive(id, active)
    
    suspend fun getRouterCount(): Int = routerDao.getRouterCount()
    
    fun getRoutersByPrefix(prefix: String): Flow<List<Router>> = routerDao.getRoutersByPrefix(prefix)
}