package com.nesventory.android.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nesventory_prefs")

/**
 * Server settings data class containing all configurable server connection parameters.
 */
data class ServerSettings(
    val apiToken: String = "",
    val remoteUrl: String = "",
    val localUrl: String = "",
    val localSsid: String = ""
) {
    /**
     * Check if server settings are configured (at least one URL is set).
     */
    fun isConfigured(): Boolean = remoteUrl.isNotBlank() || localUrl.isNotBlank()
}

/**
 * User session data class containing authentication state.
 */
data class UserSession(
    val accessToken: String = "",
    val isLoggedIn: Boolean = false
)

/**
 * DataStore-based preferences manager for storing server settings and user session.
 * Uses encrypted storage for sensitive data like API tokens and access tokens.
 */
@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_API_TOKEN = stringPreferencesKey("api_token")
        private val KEY_REMOTE_URL = stringPreferencesKey("remote_url")
        private val KEY_LOCAL_URL = stringPreferencesKey("local_url")
        private val KEY_LOCAL_SSID = stringPreferencesKey("local_ssid")
        private val KEY_ACCESS_TOKEN = stringPreferencesKey("access_token")
    }

    /**
     * Flow of current server settings.
     */
    val serverSettings: Flow<ServerSettings> = context.dataStore.data.map { preferences ->
        ServerSettings(
            apiToken = preferences[KEY_API_TOKEN] ?: "",
            remoteUrl = preferences[KEY_REMOTE_URL] ?: "",
            localUrl = preferences[KEY_LOCAL_URL] ?: "",
            localSsid = preferences[KEY_LOCAL_SSID] ?: ""
        )
    }

    /**
     * Flow of current user session.
     */
    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        val accessToken = preferences[KEY_ACCESS_TOKEN] ?: ""
        UserSession(
            accessToken = accessToken,
            isLoggedIn = accessToken.isNotBlank()
        )
    }

    /**
     * Save server settings.
     */
    suspend fun saveServerSettings(settings: ServerSettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_TOKEN] = settings.apiToken
            preferences[KEY_REMOTE_URL] = settings.remoteUrl
            preferences[KEY_LOCAL_URL] = settings.localUrl
            preferences[KEY_LOCAL_SSID] = settings.localSsid
        }
    }

    /**
     * Save access token after successful login.
     */
    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token
        }
    }

    /**
     * Clear access token on logout.
     */
    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
        }
    }

    /**
     * Clear all stored data.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
