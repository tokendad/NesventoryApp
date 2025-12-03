package com.nesventory.android.data.repository

import com.nesventory.android.data.api.NesVentoryApi
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.TokenResponse
import com.nesventory.android.data.model.User
import com.nesventory.android.data.preferences.PreferencesManager
import com.nesventory.android.data.preferences.ServerSettings
import com.nesventory.android.util.NetworkUtils
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Result wrapper for API operations.
 */
sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int? = null) : ApiResult<Nothing>()
}

/**
 * Repository for NesVentory API operations.
 * Handles automatic URL switching based on network connection.
 */
@Singleton
class NesVentoryRepository @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val networkUtils: NetworkUtils
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .build()
    }

    /**
     * Create API instance with the appropriate base URL.
     */
    private suspend fun createApi(): NesVentoryApi? {
        val settings = preferencesManager.serverSettings.first()
        val baseUrl = networkUtils.getActiveUrl(
            remoteUrl = settings.remoteUrl,
            localUrl = settings.localUrl,
            localSsid = settings.localSsid
        ) ?: return null

        val normalizedUrl = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"
        
        return Retrofit.Builder()
            .baseUrl(normalizedUrl)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(NesVentoryApi::class.java)
    }

    /**
     * Check if currently using local connection.
     */
    suspend fun isUsingLocalConnection(): Boolean {
        val settings = preferencesManager.serverSettings.first()
        return networkUtils.isOnLocalNetwork(settings.localSsid)
    }

    /**
     * Get the currently active base URL.
     */
    suspend fun getActiveBaseUrl(): String? {
        val settings = preferencesManager.serverSettings.first()
        return networkUtils.getActiveUrl(
            remoteUrl = settings.remoteUrl,
            localUrl = settings.localUrl,
            localSsid = settings.localSsid
        )
    }

    /**
     * Login with email and password.
     */
    suspend fun login(email: String, password: String): ApiResult<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            
            val response = api.login(email, password)
            
            if (response.isSuccessful) {
                val token = response.body()
                if (token != null) {
                    preferencesManager.saveAccessToken(token.accessToken)
                    ApiResult.Success(token)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                val errorMessage = when (response.code()) {
                    401 -> "Invalid email or password"
                    403 -> "Account pending approval"
                    else -> "Login failed: ${response.code()}"
                }
                ApiResult.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Logout - clear stored access token.
     */
    suspend fun logout() {
        preferencesManager.clearAccessToken()
    }

    /**
     * Get current user information.
     */
    suspend fun getCurrentUser(): ApiResult<User> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getCurrentUser("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val user = response.body()
                if (user != null) {
                    ApiResult.Success(user)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Failed to get user: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get all items.
     */
    suspend fun getItems(): ApiResult<List<Item>> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getItems("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val items = response.body() ?: emptyList()
                ApiResult.Success(items)
            } else {
                ApiResult.Error("Failed to get items: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get all locations.
     */
    suspend fun getLocations(): ApiResult<List<Location>> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getLocations("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val locations = response.body() ?: emptyList()
                ApiResult.Success(locations)
            } else {
                ApiResult.Error("Failed to get locations: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Save server settings.
     */
    suspend fun saveServerSettings(settings: ServerSettings) {
        preferencesManager.saveServerSettings(settings)
    }

    /**
     * Get current server settings.
     */
    suspend fun getServerSettings(): ServerSettings {
        return preferencesManager.serverSettings.first()
    }
}
