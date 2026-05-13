package com.tokendad.nesventory.data.preferences

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SecurePreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "nesventory_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? = prefs.getString(KEY_ACCESS_TOKEN, null)

    fun saveAccessToken(profileId: String, token: String) {
        prefs.edit()
            .putString(profileTokenKey(profileId), token)
            .putString(KEY_ACCESS_TOKEN, token)
            .apply()
    }

    fun getAccessToken(profileId: String): String? =
        prefs.getString(profileTokenKey(profileId), null)

    fun clearAccessToken() {
        prefs.edit().remove(KEY_ACCESS_TOKEN).apply()
    }

    fun deleteAccessToken(profileId: String) {
        prefs.edit().remove(profileTokenKey(profileId)).apply()
    }

    fun savePassword(password: String) {
        prefs.edit().putString(KEY_PASSWORD, password).apply()
    }

    fun getPassword(): String? = prefs.getString(KEY_PASSWORD, null)

    fun clearPassword() {
        prefs.edit().remove(KEY_PASSWORD).apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_PASSWORD = "saved_password"
        private const val KEY_ACCESS_TOKEN_PREFIX = "access_token_profile_"
    }

    private fun profileTokenKey(profileId: String): String = "$KEY_ACCESS_TOKEN_PREFIX$profileId"
}
