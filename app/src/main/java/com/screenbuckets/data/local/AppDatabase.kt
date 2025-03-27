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
                    // Initialize Vector Search Extension (SQLite-VSS)
                    Executors.newSingleThreadExecutor().execute {
                        // Load the SQLite VSS extension
                        db.execSQL("SELECT load_extension('libvss');")
                        
                        // Create virtual table for vector search
                        db.execSQL("""
                            CREATE VIRTUAL TABLE IF NOT EXISTS screenshot_vectors USING vss0(
                                embedding(1536) -- Dimension depends on your embedding model
                            );
                        """.trimIndent())
                        
                        // Create index for fast search
                        db.execSQL("CREATE INDEX IF NOT EXISTS idx_screenshot_processed ON screenshots(isProcessed);")
                    }
                }
            })
            .build()
        }
    }
}