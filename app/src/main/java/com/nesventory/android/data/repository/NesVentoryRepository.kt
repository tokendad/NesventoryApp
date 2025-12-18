package com.nesventory.android.data.repository

import com.nesventory.android.data.api.NesVentoryApi
import com.nesventory.android.data.model.AIStatus
import com.nesventory.android.data.model.Item
import com.nesventory.android.data.model.Location
import com.nesventory.android.data.model.MaintenanceTask
import com.nesventory.android.data.model.PluginStatus
import com.nesventory.android.data.model.SystemStatus
import com.nesventory.android.data.model.Tag
import com.nesventory.android.data.model.TokenResponse
import com.nesventory.android.data.model.User
import com.nesventory.android.data.model.Video
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
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
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
 * Server connection status.
 */
enum class ConnectionStatus {
    CONNECTED,          // Server is reachable and responding
    DISCONNECTED,       // Server is not reachable
    NO_NETWORK,         // No internet connection available
    NOT_CONFIGURED      // Server settings not configured
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
     * Check the current connection status to the backend server.
     * 
     * @return ConnectionStatus indicating the current state of the server connection
     */
    suspend fun checkConnectionStatus(): ConnectionStatus = withContext(Dispatchers.IO) {
        // Check if network is available
        if (!networkUtils.isNetworkAvailable()) {
            return@withContext ConnectionStatus.NO_NETWORK
        }
        
        // Get active URL
        val baseUrl = getActiveBaseUrl()
        if (baseUrl == null) {
            return@withContext ConnectionStatus.NOT_CONFIGURED
        }
        
        // Check if server is reachable
        val isReachable = networkUtils.isServerReachable(baseUrl)
        return@withContext if (isReachable) {
            ConnectionStatus.CONNECTED
        } else {
            ConnectionStatus.DISCONNECTED
        }
    }

    /**
     * Get a human-readable message for the current connection status.
     */
    fun getConnectionStatusMessage(status: ConnectionStatus): String {
        return when (status) {
            ConnectionStatus.CONNECTED -> "Connected to server"
            ConnectionStatus.DISCONNECTED -> "Server unavailable"
            ConnectionStatus.NO_NETWORK -> "No internet connection"
            ConnectionStatus.NOT_CONFIGURED -> "Server not configured"
        }
    }

    /**
     * Login with email and password.
     */
    suspend fun login(email: String, password: String): ApiResult<TokenResponse> = withContext(Dispatchers.IO) {
        try {
            // Check connection status first
            val connectionStatus = checkConnectionStatus()
            if (connectionStatus != ConnectionStatus.CONNECTED) {
                return@withContext ApiResult.Error(getConnectionStatusMessage(connectionStatus))
            }
            
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
        } catch (e: UnknownHostException) {
            ApiResult.Error("Cannot reach server - check your connection")
        } catch (e: SocketTimeoutException) {
            ApiResult.Error("Server timeout - please try again")
        } catch (e: ConnectException) {
            ApiResult.Error("Cannot connect to server")
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
        } catch (e: UnknownHostException) {
            ApiResult.Error("Cannot reach server")
        } catch (e: SocketTimeoutException) {
            ApiResult.Error("Server timeout")
        } catch (e: ConnectException) {
            ApiResult.Error("Cannot connect to server")
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
     * Get all tags.
     */
    suspend fun getTags(): ApiResult<List<Tag>> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getTags("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val tags = response.body() ?: emptyList()
                ApiResult.Success(tags)
            } else {
                ApiResult.Error("Failed to get tags: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get all maintenance tasks.
     */
    suspend fun getMaintenanceTasks(): ApiResult<List<MaintenanceTask>> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getMaintenanceTasks("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val tasks = response.body() ?: emptyList()
                ApiResult.Success(tasks)
            } else {
                ApiResult.Error("Failed to get maintenance tasks: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get all videos.
     */
    suspend fun getVideos(): ApiResult<List<Video>> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getVideos("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val videos = response.body() ?: emptyList()
                ApiResult.Success(videos)
            } else {
                ApiResult.Error("Failed to get videos: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get system status.
     * Note: This endpoint does not require authentication per backend design.
     */
    suspend fun getSystemStatus(): ApiResult<SystemStatus> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            
            val response = api.getSystemStatus()
            
            if (response.isSuccessful) {
                val status = response.body()
                if (status != null) {
                    ApiResult.Success(status)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Failed to get system status: ${response.code()}", response.code())
            }
        } catch (e: UnknownHostException) {
            ApiResult.Error("Cannot reach server")
        } catch (e: SocketTimeoutException) {
            ApiResult.Error("Server timeout")
        } catch (e: ConnectException) {
            ApiResult.Error("Cannot connect to server")
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get AI service status.
     */
    suspend fun getAIStatus(): ApiResult<AIStatus> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getAIStatus("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val status = response.body()
                if (status != null) {
                    ApiResult.Success(status)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Failed to get AI status: ${response.code()}", response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }

    /**
     * Get plugin status.
     */
    suspend fun getPluginStatus(): ApiResult<PluginStatus> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }
            
            val response = api.getPluginStatus("Bearer ${session.accessToken}")
            
            if (response.isSuccessful) {
                val status = response.body()
                if (status != null) {
                    ApiResult.Success(status)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                ApiResult.Error("Failed to get plugin status: ${response.code()}", response.code())
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

    /**
     * Process an image with AI to detect items.
     * 
     * @param imageUri URI of the image to process
     * @param context Android context for content resolver
     * @return ApiResult containing detected items or error
     */
    suspend fun processImageWithAI(imageUri: android.net.Uri, context: android.content.Context): ApiResult<com.nesventory.android.data.model.DetectionResult> = withContext(Dispatchers.IO) {
        try {
            val api = createApi() ?: return@withContext ApiResult.Error("Server not configured")
            val session = preferencesManager.userSession.first()
            
            if (!session.isLoggedIn) {
                return@withContext ApiResult.Error("Not logged in")
            }

            // Get the image data from URI
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(imageUri)
                ?: return@withContext ApiResult.Error("Failed to read image")

            val imageBytes = inputStream.use { it.readBytes() }
            
            // Determine MIME type
            val mimeType = contentResolver.getType(imageUri) ?: "image/jpeg"
            
            // Get filename from URI or generate one based on MIME type
            val filename = imageUri.lastPathSegment ?: run {
                val extension = when (mimeType) {
                    "image/png" -> "png"
                    "image/webp" -> "webp"
                    "image/gif" -> "gif"
                    else -> "jpg"
                }
                "image.$extension"
            }
            
            // Create RequestBody for the image
            val requestBody = okhttp3.RequestBody.create(
                mimeType.toMediaType(),
                imageBytes
            )
            
            // Create MultipartBody.Part for the file
            val filePart = okhttp3.MultipartBody.Part.createFormData(
                "file",
                filename,
                requestBody
            )
            
            val response = api.detectItems("Bearer ${session.accessToken}", filePart)
            
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    ApiResult.Success(result)
                } else {
                    ApiResult.Error("Empty response from server")
                }
            } else {
                val errorMessage = when (response.code()) {
                    429 -> "AI rate limit exceeded. Please try again later."
                    503 -> "AI service not configured"
                    else -> "Failed to process image: ${response.code()}"
                }
                ApiResult.Error(errorMessage, response.code())
            }
        } catch (e: Exception) {
            ApiResult.Error("Network error: ${e.message}")
        }
    }
}
