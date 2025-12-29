package com.example.nesventorynew.di

import com.example.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    // This is a placeholder and will be replaced by the interceptor
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
            // Host Selection Interceptor (Dynamic URL)
            .addInterceptor(Interceptor { chain ->
                var request = chain.request()
                
                // Fetch current settings
                val settings = runBlocking { preferencesManager.serverSettings.first() }
                val targetUrlStr = if (settings.remoteUrl.isNotBlank()) {
                     if (settings.remoteUrl.endsWith("/")) settings.remoteUrl else "${settings.remoteUrl}/"
                } else {
                    BASE_URL
                }

                val newBaseUrl = targetUrlStr.toHttpUrlOrNull()
                
                if (newBaseUrl != null) {
                    val newUrl = request.url.newBuilder()
                        .scheme(newBaseUrl.scheme)
                        .host(newBaseUrl.host)
                        .port(newBaseUrl.port)
                        .build()
                    request = request.newBuilder().url(newUrl).build()
                }

                chain.proceed(request)
            })
            // Auth Interceptor
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()

                val session = runBlocking { preferencesManager.userSession.first() }

                if (session.accessToken.isNotBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer ${session.accessToken}")
                }

                chain.proceed(requestBuilder.build())
            })
            .build()
    }

    @Provides
    @Singleton
    fun provideNesVentoryApi(
        okHttpClient: OkHttpClient
    ): NesVentoryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL) // Using a constant URL, Interceptor handles the rest
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NesVentoryApi::class.java)
    }
}