package com.screenbuckets.di

import android.content.Context
import com.screenbuckets.data.local.AppDatabase
import com.screenbuckets.data.local.ScreenshotDao
import com.screenbuckets.data.remote.LLMService
import com.screenbuckets.data.repository.ScreenshotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return AppDatabase.getInstance(context)
    }
    
    @Provides
    @Singleton
    fun provideScreenshotDao(database: AppDatabase): ScreenshotDao {
        return database.screenshotDao()
    }
    
    @Provides
    @Singleton
    fun provideScreenshotRepository(
        @ApplicationContext context: Context,
        screenshotDao: ScreenshotDao
    ): ScreenshotRepository {
        return ScreenshotRepository(context, screenshotDao)
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideLLMService(okHttpClient: OkHttpClient): LLMService {
        return Retrofit.Builder()
            .baseUrl("https://api.openai.com/") // Example LLM API URL, replace with your chosen provider
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(LLMService::class.java)
    }
}