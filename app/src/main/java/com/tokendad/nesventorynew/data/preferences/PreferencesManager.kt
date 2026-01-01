package com.tokendad.nesventorynew.data.preferences

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

// DataStore instance definition
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nesventory_prefs")

data class ServerSettings(
    val apiToken: String = "",
    val remoteUrl: String = "",
    val localUrl: String = "",
    val localSsid: String = "",
    val prioritizeLocal: Boolean = false,
    val theme: String = "system"
) {
    fun isConfigured(): Boolean = remoteUrl.isNotBlank() || localUrl.isNotBlank()
}

data class UserSession(
    val accessToken: String = "",
    val isLoggedIn: Boolean = false
)

data class SavedCredentials(
    val username: String = "",
    val password: String = "",
    val isRemembered: Boolean = false
)

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
        private val KEY_PRIORITIZE_LOCAL = androidx.datastore.preferences.core.booleanPreferencesKey("prioritize_local")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        
        // Credentials
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_PASSWORD = stringPreferencesKey("password")
        private val KEY_REMEMBER_CREDENTIALS = androidx.datastore.preferences.core.booleanPreferencesKey("remember_credentials")
    }

    val serverSettings: Flow<ServerSettings> = context.dataStore.data.map { preferences ->
        ServerSettings(
            apiToken = preferences[KEY_API_TOKEN] ?: "",
            remoteUrl = preferences[KEY_REMOTE_URL] ?: "",
            localUrl = preferences[KEY_LOCAL_URL] ?: "",
            localSsid = preferences[KEY_LOCAL_SSID] ?: "",
            prioritizeLocal = preferences[KEY_PRIORITIZE_LOCAL] ?: false,
            theme = preferences[KEY_THEME] ?: "system"
        )
    }

    val userSession: Flow<UserSession> = context.dataStore.data.map { preferences ->
        val accessToken = preferences[KEY_ACCESS_TOKEN] ?: ""
        UserSession(
            accessToken = accessToken,
            isLoggedIn = accessToken.isNotBlank()
        )
    }

    val savedCredentials: Flow<SavedCredentials> = context.dataStore.data.map { preferences ->
        SavedCredentials(
            username = preferences[KEY_USERNAME] ?: "",
            password = preferences[KEY_PASSWORD] ?: "",
            isRemembered = preferences[KEY_REMEMBER_CREDENTIALS] ?: false
        )
    }

    suspend fun saveServerSettings(settings: ServerSettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_API_TOKEN] = settings.apiToken
            preferences[KEY_REMOTE_URL] = settings.remoteUrl
            preferences[KEY_LOCAL_URL] = settings.localUrl
            preferences[KEY_LOCAL_SSID] = settings.localSsid
            preferences[KEY_PRIORITIZE_LOCAL] = settings.prioritizeLocal
            preferences[KEY_THEME] = settings.theme
        }
    }

    suspend fun saveCredentials(username: String, password: String, remember: Boolean) {
        context.dataStore.edit { preferences ->
            if (remember) {
                preferences[KEY_USERNAME] = username
                preferences[KEY_PASSWORD] = password
                preferences[KEY_REMEMBER_CREDENTIALS] = true
            } else {
                preferences.remove(KEY_USERNAME)
                preferences.remove(KEY_PASSWORD)
                preferences[KEY_REMEMBER_CREDENTIALS] = false
            }
        }
    }

    suspend fun saveAccessToken(token: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = token
        }
    }

    suspend fun clearAccessToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
        }
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}