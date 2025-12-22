package com.example.nesventorynew.di

import com.example.nesventorynew.data.preferences.PreferencesManager
import com.example.nesventorynew.data.remote.NesVentoryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Hardcoded Base URL for the test environment
    private const val BASE_URL = "https://nesdemo.welshrd.com/"

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        preferencesManager: PreferencesManager
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                // Still fetch the session token so we can stay logged in
                val session = runBlocking { preferencesManager.userSession.first() }

                // Add Authorization header if we have a token
                if (session.accessToken.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${session.accessToken}")
                }

                chain.proceed(requestBuilder.build())
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideNesVentoryApi(okHttpClient: OkHttpClient): NesVentoryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Using the hardcoded URL directly
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NesVentoryApi::class.java)
    }
}