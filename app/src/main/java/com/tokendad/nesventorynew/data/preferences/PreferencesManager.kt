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
    val theme: String = "system",
    val printMethod: String = "local", // "local" or "server"
    val localPrinterModel: String = "D11_H", // Default to D11-H
    val localPrinterDensity: Int = 3 // Print density 1-5, default 3
) {
    fun isConfigured(): Boolean = remoteUrl.isNotBlank() || localUrl.isNotBlank()
}

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private val KEY_API_TOKEN = stringPreferencesKey("api_token")
        private val KEY_REMOTE_URL = stringPreferencesKey("remote_url")
        private val KEY_LOCAL_URL = stringPreferencesKey("local_url")
        private val KEY_LOCAL_SSID = stringPreferencesKey("local_ssid")
        private val KEY_PRIORITIZE_LOCAL = androidx.datastore.preferences.core.booleanPreferencesKey("prioritize_local")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_PRINT_METHOD = stringPreferencesKey("print_method")
        private val KEY_LOCAL_PRINTER_MODEL = stringPreferencesKey("local_printer_model")
        private val KEY_LOCAL_PRINTER_DENSITY = androidx.datastore.preferences.core.intPreferencesKey("local_printer_density")

        // Credentials
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_REMEMBER_CREDENTIALS = androidx.datastore.preferences.core.booleanPreferencesKey("remember_credentials")
    }

    val serverSettings: Flow<ServerSettings> = context.dataStore.data.map { preferences ->
        ServerSettings(
            apiToken = preferences[KEY_API_TOKEN] ?: "",
            remoteUrl = preferences[KEY_REMOTE_URL] ?: "",
            localUrl = preferences[KEY_LOCAL_URL] ?: "",
            localSsid = preferences[KEY_LOCAL_SSID] ?: "",
            prioritizeLocal = preferences[KEY_PRIORITIZE_LOCAL] ?: false,
            theme = preferences[KEY_THEME] ?: "system",
            printMethod = preferences[KEY_PRINT_METHOD] ?: "local",
            localPrinterModel = preferences[KEY_LOCAL_PRINTER_MODEL] ?: "D11_H",
            localPrinterDensity = preferences[KEY_LOCAL_PRINTER_DENSITY] ?: 3
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
            preferences[KEY_PRINT_METHOD] = settings.printMethod
            preferences[KEY_LOCAL_PRINTER_MODEL] = settings.localPrinterModel
            preferences[KEY_LOCAL_PRINTER_DENSITY] = settings.localPrinterDensity
        }
    }

    // Username-only storage (password moved to SecurePreferencesManager)
    suspend fun saveUsername(username: String, remember: Boolean) {
        context.dataStore.edit { preferences ->
            if (remember) {
                preferences[KEY_USERNAME] = username
                preferences[KEY_REMEMBER_CREDENTIALS] = true
            } else {
                preferences.remove(KEY_USERNAME)
                preferences[KEY_REMEMBER_CREDENTIALS] = false
            }
        }
    }

    data class SavedUsername(val username: String = "", val isRemembered: Boolean = false)

    val savedUsername: Flow<SavedUsername> = context.dataStore.data.map { preferences ->
        SavedUsername(
            username = preferences[KEY_USERNAME] ?: "",
            isRemembered = preferences[KEY_REMEMBER_CREDENTIALS] ?: false
        )
    }

    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}