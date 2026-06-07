package com.example.ai_data.di

import android.util.Log
import com.example.ai_data.remote.AiApiService
import com.example.ai_data.repository.AiAgentRepositoryImpl
import com.example.ai_domain.repository.AiAgentRepository
import com.example.ai_data.BuildConfig
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindModule {

    @Binds
    @Singleton
    abstract fun bindAiAgentRepository(
        impl: AiAgentRepositoryImpl
    ): AiAgentRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DataProvideModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        Log.i("DataProvideModule", "provideOkHttpClient called")
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request()
                Log.i("AiApiHttp", "Request -> ${request.method} ${request.url}")
                val response = chain.proceed(request)
                Log.i("AiApiHttp", "Response <- ${response.code} ${response.message} ${request.url}")
                response
            }
            .build()
    }

    @Provides
    @Singleton
    fun provideAiApiService(client: OkHttpClient): AiApiService {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.AI_BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AiApiService::class.java)
    }
}
