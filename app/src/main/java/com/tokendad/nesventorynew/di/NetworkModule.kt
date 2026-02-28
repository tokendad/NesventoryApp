package com.tokendad.nesventorynew.di

import com.tokendad.nesventorynew.data.preferences.PreferencesManager
import com.tokendad.nesventorynew.data.preferences.SecurePreferencesManager
import com.tokendad.nesventorynew.data.remote.NesVentoryApi
import com.tokendad.nesventorynew.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    
    private const val BASE_URL = "${Constants.DEFAULT_REMOTE_URL}/"

    @Volatile private var cachedToken: String? = null
    @Volatile private var cachedRemoteUrl: String = ""
    @Volatile private var cachedLocalUrl: String = ""
    @Volatile private var cachedPrioritizeLocal: Boolean = false
    @Volatile private var cachedLocalSsid: String = ""

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
        preferencesManager: PreferencesManager,
        securePreferencesManager: SecurePreferencesManager,
        applicationScope: CoroutineScope
    ): OkHttpClient {
        // Cache settings and token via background coroutines (no runBlocking)
        applicationScope.launch {
            preferencesManager.serverSettings.collect { settings ->
                cachedRemoteUrl = settings.remoteUrl
                cachedLocalUrl = settings.localUrl
                cachedPrioritizeLocal = settings.prioritizeLocal
                cachedLocalSsid = settings.localSsid
            }
        }
        // Load token synchronously to avoid race with first network request
        cachedToken = securePreferencesManager.getAccessToken()

        return OkHttpClient.Builder()
            // 1. Host Selection Interceptor
            .addInterceptor(Interceptor { chain ->
                var request = chain.request()
                val currentHost = request.url.host
                val placeholderHost = "nesdemo.welshrd.com"
                
                if (currentHost == placeholderHost) {
                    val targetUrlStr = if (cachedRemoteUrl.isNotBlank()) {
                        cachedRemoteUrl
                    } else if (cachedLocalUrl.isNotBlank()) {
                        cachedLocalUrl
                    } else {
                        null
                    }
                    
                    if (targetUrlStr != null) {
                        val safeTarget = if (targetUrlStr.endsWith("/")) targetUrlStr else "$targetUrlStr/"
                        val newBaseUrl = safeTarget.toHttpUrlOrNull()
                        
                        if (newBaseUrl != null) {
                            val apiPath = request.url.encodedPath.trimStart('/')
                            val newUrl = newBaseUrl.newBuilder()
                                .addEncodedPathSegments(apiPath)
                                .query(request.url.query)
                                .build()
                            request = request.newBuilder().url(newUrl).build()
                        }
                    }
                }
                chain.proceed(request)
            })
            // 2. Auth Header Interceptor (reads cached token - no blocking)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                val token = cachedToken
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            })
            // 3. 401 Unauthorized Interceptor (cache-only; actual clear deferred)
            .addInterceptor(Interceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 401) {
                    cachedToken = null
                    applicationScope.launch { securePreferencesManager.clearAccessToken() }
                }
                response
            })
            // 4. Logging Interceptor
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideNesVentoryApi(okHttpClient: OkHttpClient): NesVentoryApi {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NesVentoryApi::class.java)
    }

    /**
     * Updates the cached token. Called after login/logout.
     */
    fun updateCachedToken(token: String?) {
        cachedToken = token
    }
}