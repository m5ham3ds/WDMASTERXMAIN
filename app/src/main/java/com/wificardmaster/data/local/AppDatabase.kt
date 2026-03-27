package com.wdmaster.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.wdmaster.app.data.model.CardPattern
import com.wdmaster.app.data.model.Router
import com.wdmaster.app.data.model.TestResult

@Database(
    entities = [Router::class, CardPattern::class, TestResult::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun routerDao(): RouterDao
    abstract fun patternDao(): PatternDao
    abstract fun resultDao(): ResultDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "wdmaster_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}