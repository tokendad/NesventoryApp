package com.nesventory.android.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for network-related operations.
 * Handles WiFi SSID detection for automatic URL switching between local and remote servers.
 */
@Singleton
class NetworkUtils @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    
    private val wifiManager: WifiManager =
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

    /**
     * Check if the device is connected to the internet.
     */
    fun isNetworkAvailable(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    /**
     * Check if the device is connected to WiFi.
     */
    fun isWifiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    /**
     * Get the current WiFi SSID.
     * Note: On Android 10+ (API 29+), requires ACCESS_FINE_LOCATION permission and location services
     * to be enabled for reliable SSID retrieval. Without these, the SSID may return as "<unknown ssid>".
     * 
     * For a more robust implementation in production, consider using ConnectivityManager.NetworkCallback
     * with NetworkCapabilities.NET_CAPABILITY_INTERNET to monitor network changes.
     */
    @Suppress("DEPRECATION")
    fun getCurrentSsid(): String? {
        if (!isWifiConnected()) return null
        
        // Note: On Android 10+, this requires ACCESS_FINE_LOCATION permission
        // The app should request this permission before calling this method
        val wifiInfo = wifiManager.connectionInfo
        val ssid = wifiInfo?.ssid
        
        // Remove quotes from SSID if present
        return ssid?.trim()?.removeSurrounding("\"")?.takeIf { 
            it.isNotBlank() && it != "<unknown ssid>" 
        }
    }

    /**
     * Determine which URL to use based on current WiFi connection.
     * Returns local URL if connected to the specified SSID, otherwise returns remote URL.
     * 
     * @param remoteUrl The remote server URL
     * @param localUrl The local server URL
     * @param localSsid The SSID that should trigger local URL usage
     * @return The appropriate URL to use, or null if neither is configured
     */
    fun getActiveUrl(remoteUrl: String, localUrl: String, localSsid: String): String? {
        // If no URLs are configured, return null
        if (remoteUrl.isBlank() && localUrl.isBlank()) return null
        
        // If only remote URL is configured, use it
        if (localUrl.isBlank() || localSsid.isBlank()) {
            return remoteUrl.takeIf { it.isNotBlank() }
        }
        
        // If only local URL is configured, use it
        if (remoteUrl.isBlank()) {
            return localUrl
        }
        
        // Check if we're on the local network
        val currentSsid = getCurrentSsid()
        return if (currentSsid != null && currentSsid.equals(localSsid, ignoreCase = true)) {
            localUrl
        } else {
            remoteUrl
        }
    }

    /**
     * Check if currently connected to the local network.
     */
    fun isOnLocalNetwork(localSsid: String): Boolean {
        if (localSsid.isBlank()) return false
        val currentSsid = getCurrentSsid() ?: return false
        return currentSsid.equals(localSsid, ignoreCase = true)
    }
}
