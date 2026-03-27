package com.wdmaster.app.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import com.wdmaster.app.data.model.Router
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WiFiUtils @Inject constructor(
    private val context: Context
) {

    private val wifiManager: WifiManager by lazy {
        context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val connectivityManager: ConnectivityManager by lazy {
        context.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    }

    fun isWiFiEnabled(): Boolean {
        return wifiManager.isWifiEnabled
    }

    fun isWiFiConnected(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
    }

    fun getConnectedWiFiName(): String? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return null
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return null
            val ssid = capabilities.transportInfo as? android.net.wifi.WifiInfo
            return ssid?.ssid?.removeSurrounding("\"")
        }
        return wifiManager.connectionInfo?.ssid?.removeSurrounding("\"")
    }

    fun getConnectedWiFiBSSID(): String? {
        return wifiManager.connectionInfo?.bssid
    }

    fun getWiFiSignalStrength(): Int {
        return wifiManager.connectionInfo?.rssi ?: 0
    }

    fun calculateSignalLevel(rssi: Int): Int {
        return WifiManager.calculateSignalLevel(rssi, 5)
    }

    fun isMACPrefixMatch(macAddress: String?, prefix: String): Boolean {
        if (macAddress == null) return false
        return macAddress.uppercase().startsWith(prefix.uppercase())
    }

    fun scanNearbyRouters(): List<Router> {
        // Requires WiFi scan permission and implementation
        // This is a placeholder for actual WiFi scanning
        return emptyList()
    }

    fun enableWiFi(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Cannot enable WiFi programmatically on Android 10+
            return false
        }
        return wifiManager.setWifiEnabled(true)
    }

    fun disableWiFi(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Cannot disable WiFi programmatically on Android 10+
            return false
        }
        return wifiManager.setWifiEnabled(false)
    }

    fun getWiFiIPAddress(): String? {
        val ip = wifiManager.connectionInfo?.ipAddress ?: return null
        return "${ip and 0xFF}.${ip shr 8 and 0xFF}.${ip shr 16 and 0xFF}.${ip shr 24 and 0xFF}"
    }

    fun getWiFiSubnetMask(): String? {
        // Requires additional implementation
        return null
    }
}

// Extension function to remove surrounding quotes
private fun String.removeSurrounding(quote: String): String {
    return removePrefix(quote).removeSuffix(quote)
}