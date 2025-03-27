package com.screenbuckets.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.screenbuckets.data.model.Screenshot
import com.screenbuckets.data.model.ScreenshotConverters
import java.util.concurrent.Executors

@Database(
    entities = [Screenshot::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ScreenshotConverters::class)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun screenshotDao(): ScreenshotDao
    
    companion object {
        private const val DATABASE_NAME = "screenbuckets.db"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }
        }
        
        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                DATABASE_NAME
            )
            .fallbackToDestructiveMigration()
            .addCallback(object : RoomDatabase.Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // Initialize basic database structure
                    Executors.newSingleThreadExecutor().execute {
                        // Create index for faster processing
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_screenshot_processed ON screenshots(isProcessed);")
                    }
                }
            })
            .build()
        }
    }
}