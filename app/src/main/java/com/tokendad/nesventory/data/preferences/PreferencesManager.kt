package com.tokendad.nesventory.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore instance definition
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "nesventory_prefs")

data class ServerSettings(
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
    private val gson = Gson()

    companion object {
        private val KEY_REMOTE_URL = stringPreferencesKey("remote_url")
        private val KEY_LOCAL_URL = stringPreferencesKey("local_url")
        private val KEY_LOCAL_SSID = stringPreferencesKey("local_ssid")
        private val KEY_PRIORITIZE_LOCAL = booleanPreferencesKey("prioritize_local")
        private val KEY_THEME = stringPreferencesKey("app_theme")
        private val KEY_PRINT_METHOD = stringPreferencesKey("print_method")
        private val KEY_LOCAL_PRINTER_MODEL = stringPreferencesKey("local_printer_model")
        private val KEY_LOCAL_PRINTER_DENSITY = intPreferencesKey("local_printer_density")

        // Credentials
        private val KEY_USERNAME = stringPreferencesKey("username")
        private val KEY_REMEMBER_CREDENTIALS = booleanPreferencesKey("remember_credentials")

        // Multi-server profiles
        private val KEY_SERVER_PROFILES_JSON = stringPreferencesKey("server_profiles_json")
        private val KEY_ACTIVE_PROFILE_ID = stringPreferencesKey("active_server_profile_id")
        private val KEY_PROFILES_MIGRATED = booleanPreferencesKey("server_profiles_migrated")
    }

    private fun parseServerProfiles(json: String?): List<ServerProfile> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            gson.fromJson<List<ServerProfile>>(json, TypeToken.getParameterized(List::class.java, ServerProfile::class.java).type)
        }.getOrDefault(emptyList())
    }

    val serverProfiles: Flow<ServerProfileList> = context.dataStore.data.map { preferences ->
        val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON])
        val activeProfileId = preferences[KEY_ACTIVE_PROFILE_ID]
        ServerProfileList(
            profiles = profiles,
            activeProfileId = when {
                profiles.isEmpty() -> null
                activeProfileId != null && profiles.any { it.id == activeProfileId } -> activeProfileId
                else -> profiles.first().id
            }
        )
    }

    val serverSettings: Flow<ServerSettings> = context.dataStore.data.map { preferences ->
        val profileList = ServerProfileList(
            profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON]),
            activeProfileId = preferences[KEY_ACTIVE_PROFILE_ID]
        )
        val activeProfile = profileList.activeProfile
        ServerSettings(
            remoteUrl = activeProfile?.remoteUrl ?: preferences[KEY_REMOTE_URL].orEmpty(),
            localUrl = activeProfile?.localUrl ?: preferences[KEY_LOCAL_URL].orEmpty(),
            localSsid = activeProfile?.localSsid ?: preferences[KEY_LOCAL_SSID].orEmpty(),
            prioritizeLocal = activeProfile?.prioritizeLocal ?: (preferences[KEY_PRIORITIZE_LOCAL] ?: false),
            theme = preferences[KEY_THEME] ?: "system",
            printMethod = preferences[KEY_PRINT_METHOD] ?: "local",
            localPrinterModel = preferences[KEY_LOCAL_PRINTER_MODEL] ?: "D11_H",
            localPrinterDensity = preferences[KEY_LOCAL_PRINTER_DENSITY] ?: 3
        )
    }

    suspend fun saveServerSettings(settings: ServerSettings) {
        context.dataStore.edit { preferences ->
            preferences[KEY_REMOTE_URL] = settings.remoteUrl
            preferences[KEY_LOCAL_URL] = settings.localUrl
            preferences[KEY_LOCAL_SSID] = settings.localSsid
            preferences[KEY_PRIORITIZE_LOCAL] = settings.prioritizeLocal
            preferences[KEY_THEME] = settings.theme
            preferences[KEY_PRINT_METHOD] = settings.printMethod
            preferences[KEY_LOCAL_PRINTER_MODEL] = settings.localPrinterModel
            preferences[KEY_LOCAL_PRINTER_DENSITY] = settings.localPrinterDensity

            val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON]).toMutableList()
            val activeProfileId = preferences[KEY_ACTIVE_PROFILE_ID]
            val activeIndex = profiles.indexOfFirst { it.id == activeProfileId }
            if (activeIndex >= 0) {
                profiles[activeIndex] = profiles[activeIndex].copy(
                    remoteUrl = settings.remoteUrl,
                    localUrl = settings.localUrl,
                    localSsid = settings.localSsid,
                    prioritizeLocal = settings.prioritizeLocal
                )
            } else if (profiles.isEmpty() && settings.isConfigured()) {
                val profile = ServerProfile(
                    name = "Default",
                    remoteUrl = settings.remoteUrl,
                    localUrl = settings.localUrl,
                    localSsid = settings.localSsid,
                    prioritizeLocal = settings.prioritizeLocal
                )
                profiles += profile
                preferences[KEY_ACTIVE_PROFILE_ID] = profile.id
            }
            if (profiles.isNotEmpty()) {
                preferences[KEY_SERVER_PROFILES_JSON] = gson.toJson(profiles)
            }
        }
    }

    suspend fun saveServerProfiles(profileList: ServerProfileList) {
        context.dataStore.edit { preferences ->
            preferences[KEY_SERVER_PROFILES_JSON] = gson.toJson(profileList.profiles)
            val selected = profileList.activeProfileId
                ?: profileList.profiles.firstOrNull()?.id
            if (selected == null) {
                preferences.remove(KEY_ACTIVE_PROFILE_ID)
            } else {
                preferences[KEY_ACTIVE_PROFILE_ID] = selected
            }
        }
    }

    suspend fun setActiveProfile(profileId: String) {
        context.dataStore.edit { preferences ->
            val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON])
            if (profiles.any { it.id == profileId }) {
                preferences[KEY_ACTIVE_PROFILE_ID] = profileId
                val active = profiles.first { it.id == profileId }
                preferences[KEY_REMOTE_URL] = active.remoteUrl
                preferences[KEY_LOCAL_URL] = active.localUrl
                preferences[KEY_LOCAL_SSID] = active.localSsid
                preferences[KEY_PRIORITIZE_LOCAL] = active.prioritizeLocal
            }
        }
    }

    suspend fun upsertServerProfile(profile: ServerProfile, makeActive: Boolean = false) {
        context.dataStore.edit { preferences ->
            val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON]).toMutableList()
            val index = profiles.indexOfFirst { it.id == profile.id }
            if (index >= 0) {
                profiles[index] = profile
            } else {
                profiles += profile
            }
            preferences[KEY_SERVER_PROFILES_JSON] = gson.toJson(profiles)

            val shouldActivate = makeActive || preferences[KEY_ACTIVE_PROFILE_ID].isNullOrBlank()
            if (shouldActivate) {
                preferences[KEY_ACTIVE_PROFILE_ID] = profile.id
                preferences[KEY_REMOTE_URL] = profile.remoteUrl
                preferences[KEY_LOCAL_URL] = profile.localUrl
                preferences[KEY_LOCAL_SSID] = profile.localSsid
                preferences[KEY_PRIORITIZE_LOCAL] = profile.prioritizeLocal
            }
        }
    }

    suspend fun deleteServerProfile(profileId: String) {
        context.dataStore.edit { preferences ->
            val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON]).toMutableList()
            val removed = profiles.removeAll { it.id == profileId }
            if (!removed) return@edit

            preferences[KEY_SERVER_PROFILES_JSON] = gson.toJson(profiles)
            val activeProfileId = preferences[KEY_ACTIVE_PROFILE_ID]
            if (activeProfileId == profileId) {
                val newActive = profiles.firstOrNull()
                if (newActive == null) {
                    preferences.remove(KEY_ACTIVE_PROFILE_ID)
                    preferences[KEY_REMOTE_URL] = ""
                    preferences[KEY_LOCAL_URL] = ""
                    preferences[KEY_LOCAL_SSID] = ""
                    preferences[KEY_PRIORITIZE_LOCAL] = false
                } else {
                    preferences[KEY_ACTIVE_PROFILE_ID] = newActive.id
                    preferences[KEY_REMOTE_URL] = newActive.remoteUrl
                    preferences[KEY_LOCAL_URL] = newActive.localUrl
                    preferences[KEY_LOCAL_SSID] = newActive.localSsid
                    preferences[KEY_PRIORITIZE_LOCAL] = newActive.prioritizeLocal
                }
            }
        }
    }

    suspend fun migrateLegacyServerSettingsIfNeeded() {
        context.dataStore.edit { preferences ->
            if (preferences[KEY_PROFILES_MIGRATED] == true) {
                return@edit
            }

            val profiles = parseServerProfiles(preferences[KEY_SERVER_PROFILES_JSON]).toMutableList()
            if (profiles.isEmpty()) {
                val remoteUrl = preferences[KEY_REMOTE_URL].orEmpty()
                val localUrl = preferences[KEY_LOCAL_URL].orEmpty()
                val localSsid = preferences[KEY_LOCAL_SSID].orEmpty()
                val prioritizeLocal = preferences[KEY_PRIORITIZE_LOCAL] ?: false
                if (remoteUrl.isNotBlank() || localUrl.isNotBlank()) {
                    val defaultProfile = ServerProfile(
                        name = "Default",
                        remoteUrl = remoteUrl,
                        localUrl = localUrl,
                        localSsid = localSsid,
                        prioritizeLocal = prioritizeLocal
                    )
                    profiles += defaultProfile
                    preferences[KEY_ACTIVE_PROFILE_ID] = defaultProfile.id
                    preferences[KEY_SERVER_PROFILES_JSON] = gson.toJson(profiles)
                }
            }
            preferences[KEY_PROFILES_MIGRATED] = true
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