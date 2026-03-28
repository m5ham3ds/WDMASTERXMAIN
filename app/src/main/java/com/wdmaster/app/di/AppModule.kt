package com.wdmaster.app.di

import android.content.Context
import com.wdmaster.app.data.local.AppDatabase
import com.wdmaster.app.data.repository.*
import com.wdmaster.app.domain.learning.PatternLearningSystem
import com.wdmaster.app.service.TestService
import com.wdmaster.app.service.TestServiceBridge
import com.wdmaster.app.utils.TerminalLogger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getDatabase(context)
    }
    
    @Provides
    @Singleton
    fun provideRouterDao(db: AppDatabase) = db.routerDao()
    
    @Provides
    @Singleton
    fun providePatternDao(db: AppDatabase) = db.patternDao()
    
    @Provides
    @Singleton
    fun provideResultDao(db: AppDatabase) = db.resultDao()
    
    @Provides
    @Singleton
    fun provideSettingsRepository(@ApplicationContext context: Context) = 
        SettingsRepository(context)
    
    @Provides
    @Singleton
    fun provideRouterRepository(db: AppDatabase) = 
        RouterRepository(db.routerDao())
    
    @Provides
    @Singleton
    fun providePatternRepository(db: AppDatabase) = 
        PatternRepository(db.patternDao())
    
    @Provides
    @Singleton
    fun provideResultRepository(db: AppDatabase) = 
        ResultRepository(db.resultDao())
    
    @Provides
    @Singleton
    fun provideLearningSystem(patternDao: com.wdmaster.app.data.local.PatternDao) = 
        PatternLearningSystem(patternDao)
    
    @Provides
    @Singleton
    fun provideTerminalLogger(): TerminalLogger = TerminalLogger()
}