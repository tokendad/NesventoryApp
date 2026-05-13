package com.tokendad.nesventory.di

import com.tokendad.nesventory.BuildConfig
import com.tokendad.nesventory.data.preferences.PreferencesManager
import com.tokendad.nesventory.data.preferences.SecurePreferencesManager
import com.tokendad.nesventory.data.remote.NesVentoryApi
import com.tokendad.nesventory.network.ForbiddenEventBus
import com.tokendad.nesventory.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Interceptor
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.InetAddress
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    private val BASE_URL = "${Constants.DEFAULT_REMOTE_URL}/"

    @Volatile private var cachedToken: String? = null
    @Volatile private var cachedRemoteUrl: String = ""
    @Volatile private var cachedLocalUrl: String = ""
    @Volatile private var cachedPrioritizeLocal: Boolean = false
    @Volatile private var cachedLocalSsid: String = ""

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    private fun isAllowedCleartext(url: HttpUrl): Boolean {
        if (!url.isHttps) {
            val host = url.host
            if (host.equals("localhost", ignoreCase = true) || host == "127.0.0.1") {
                return true
            }
            return try {
                val address = InetAddress.getByName(host)
                address.isAnyLocalAddress ||
                    address.isLoopbackAddress ||
                    address.isSiteLocalAddress
            } catch (_: Exception) {
                false
            }
        }
        return true
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        preferencesManager: PreferencesManager,
        securePreferencesManager: SecurePreferencesManager,
        forbiddenEventBus: ForbiddenEventBus,
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
                            if (!isAllowedCleartext(newBaseUrl)) {
                                throw IOException("Cleartext HTTP is only allowed for localhost and private LAN hosts.")
                            }
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
            // 4. Forbidden Interceptor
            .addInterceptor(Interceptor { chain ->
                val response = chain.proceed(chain.request())
                if (response.code == 403) {
                    forbiddenEventBus.emitForbidden()
                }
                response
            })
            // 5. Logging Interceptor
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