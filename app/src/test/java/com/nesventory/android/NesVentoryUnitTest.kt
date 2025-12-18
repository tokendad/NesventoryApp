package com.nesventory.android

import com.nesventory.android.data.repository.ConnectionStatus
import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for NesVentory Android app.
 */
class NesVentoryUnitTest {
    
    @Test
    fun serverSettings_isConfigured_returnsTrueWhenRemoteUrlSet() {
        val settings = com.nesventory.android.data.preferences.ServerSettings(
            apiToken = "",
            remoteUrl = "https://example.com",
            localUrl = "",
            localSsid = ""
        )
        assertTrue(settings.isConfigured())
    }

    @Test
    fun serverSettings_isConfigured_returnsTrueWhenLocalUrlSet() {
        val settings = com.nesventory.android.data.preferences.ServerSettings(
            apiToken = "",
            remoteUrl = "",
            localUrl = "http://192.168.1.100:8000",
            localSsid = ""
        )
        assertTrue(settings.isConfigured())
    }

    @Test
    fun serverSettings_isConfigured_returnsFalseWhenNoUrlsSet() {
        val settings = com.nesventory.android.data.preferences.ServerSettings(
            apiToken = "token",
            remoteUrl = "",
            localUrl = "",
            localSsid = "MyNetwork"
        )
        assertFalse(settings.isConfigured())
    }

    @Test
    fun serverSettings_defaultValues_areEmpty() {
        val settings = com.nesventory.android.data.preferences.ServerSettings()
        assertEquals("", settings.apiToken)
        assertEquals("", settings.remoteUrl)
        assertEquals("", settings.localUrl)
        assertEquals("", settings.localSsid)
    }

    @Test
    fun connectionStatus_enumValues_exist() {
        // Verify all connection status enum values are defined
        assertNotNull(ConnectionStatus.CONNECTED)
        assertNotNull(ConnectionStatus.DISCONNECTED)
        assertNotNull(ConnectionStatus.NO_NETWORK)
        assertNotNull(ConnectionStatus.NOT_CONFIGURED)
    }

    @Test
    fun connectionStatus_enumValues_count() {
        // Verify we have exactly 4 connection status values
        val values = ConnectionStatus.values()
        assertEquals(4, values.size)
    }
}
