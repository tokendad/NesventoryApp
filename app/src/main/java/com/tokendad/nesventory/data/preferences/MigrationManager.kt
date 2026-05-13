package com.tokendad.nesventory.data.preferences

import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MigrationManager @Inject constructor(
    private val preferencesManager: PreferencesManager
) {
    suspend fun runMigrations() {
        preferencesManager.migrateLegacyServerSettingsIfNeeded()
    }
}
