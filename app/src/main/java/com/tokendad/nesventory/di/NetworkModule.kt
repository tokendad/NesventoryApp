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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    @Volatile private var cachedNetworkState = NetworkState(
        profileId = null,
        remoteUrl = "",
        localUrl = "",
        prioritizeLocal = false,
        localSsid = "",
        token = null
    )
    
    private data class NetworkState(
        val profileId: String?,
        val remoteUrl: String,
        val localUrl: String,
        val prioritizeLocal: Boolean,
        val localSsid: String,
        val token: String?
    )

    private data class RequestAuthContext(
        val profileId: String?,
        val token: String?,
        val usedLegacyToken: Boolean
    )

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
        val initialState = runBlocking {
            val profileList = preferencesManager.serverProfiles.first()
            val active = profileList.activeProfile
            var resolvedToken = when {
                active != null -> securePreferencesManager.getAccessToken(active.id)
                profileList.profiles.isEmpty() -> securePreferencesManager.getAccessToken()
                else -> null
            }
            if (active == null && profileList.profiles.isNotEmpty()) {
                resolvedToken = null
            }
            NetworkState(
                profileId = active?.id,
                remoteUrl = active?.remoteUrl.orEmpty(),
                localUrl = active?.localUrl.orEmpty(),
                prioritizeLocal = active?.prioritizeLocal ?: false,
                localSsid = active?.localSsid.orEmpty(),
                token = resolvedToken
            )
        }
        cachedNetworkState = initialState

        // Keep settings and token in sync with profile changes.
        applicationScope.launch {
            preferencesManager.serverProfiles.collect { profileList ->
                val active = profileList.activeProfile
                var resolvedToken = when {
                    active != null -> securePreferencesManager.getAccessToken(active.id)
                    profileList.profiles.isEmpty() -> securePreferencesManager.getAccessToken()
                    else -> null
                }
                if (active == null && profileList.profiles.isNotEmpty()) {
                    // Never use a token from a different profile context.
                    resolvedToken = null
                }
                cachedNetworkState = NetworkState(
                    profileId = active?.id,
                    remoteUrl = active?.remoteUrl.orEmpty(),
                    localUrl = active?.localUrl.orEmpty(),
                    prioritizeLocal = active?.prioritizeLocal ?: false,
                    localSsid = active?.localSsid.orEmpty(),
                    token = resolvedToken
                )
            }
        }

        return OkHttpClient.Builder()
            // 1. Host Selection Interceptor
            .addInterceptor(Interceptor { chain ->
                var request = chain.request()
                val networkState = cachedNetworkState
                val currentHost = request.url.host
                val placeholderHost = "nesdemo.welshrd.com"
                
                if (currentHost == placeholderHost) {
                    val targetUrlStr = if (networkState.remoteUrl.isNotBlank()) {
                        networkState.remoteUrl
                    } else if (networkState.localUrl.isNotBlank()) {
                        networkState.localUrl
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
                request = request.newBuilder()
                    .tag(
                        RequestAuthContext::class.java,
                        RequestAuthContext(
                            profileId = networkState.profileId,
                            token = networkState.token,
                            usedLegacyToken = networkState.profileId == null && !networkState.token.isNullOrBlank()
                        )
                    )
                    .build()
                chain.proceed(request)
            })
            // 2. Auth Header Interceptor (reads cached token - no blocking)
            .addInterceptor(Interceptor { chain ->
                val originalRequest = chain.request()
                val requestBuilder = originalRequest.newBuilder()
                val authContext = originalRequest.tag(RequestAuthContext::class.java)
                val token = authContext?.token
                if (!token.isNullOrBlank()) {
                    requestBuilder.addHeader("Authorization", "Bearer $token")
                }
                chain.proceed(requestBuilder.build())
            })
            // 3. 401 Unauthorized Interceptor (cache-only; actual clear deferred)
            .addInterceptor(Interceptor { chain ->
                val request = chain.request()
                val response = chain.proceed(request)
                if (response.code == 401) {
                    val authContext = request.tag(RequestAuthContext::class.java)
                    val requestProfileId = authContext?.profileId
                    applicationScope.launch {
                        when {
                            requestProfileId != null -> {
                                securePreferencesManager.deleteAccessToken(requestProfileId)
                                if (cachedNetworkState.profileId == requestProfileId) {
                                    cachedNetworkState = cachedNetworkState.copy(token = null)
                                }
                            }
                            authContext?.usedLegacyToken == true -> {
                                securePreferencesManager.clearAccessToken()
                                if (cachedNetworkState.profileId == null) {
                                    cachedNetworkState = cachedNetworkState.copy(token = null)
                                }
                            }
                        }
                    }
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
        cachedNetworkState = cachedNetworkState.copy(token = token)
    }

    fun updateCachedToken(profileId: String?, token: String?) {
        cachedNetworkState = cachedNetworkState.copy(profileId = profileId, token = token)
    }
}