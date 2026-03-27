package com.wdmaster.app.utils

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.wdmaster.app.R
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PermissionHelper @Inject constructor() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
        const val NOTIFICATION_PERMISSION_CODE = 1002
        const val LOCATION_PERMISSION_CODE = 1003
    }

    private val requiredPermissions: Array<String>
        get() {
            val permissions = mutableListOf(
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.INTERNET,
                Manifest.permission.FOREGROUND_SERVICE,
                Manifest.permission.WAKE_LOCK
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            }

            return permissions.toTypedArray()
        }

    fun hasAllPermissions(context: Context): Boolean {
        return requiredPermissions.all {
            ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }
    fun getMissingPermissions(context: Context): List<String> {
        return requiredPermissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }
    }

    fun requestPermissions(activity: Activity) {
        val missingPermissions = getMissingPermissions(activity)
        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    fun requestNotificationPermission(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    activity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    NOTIFICATION_PERMISSION_CODE
                )
            }
        }
    }

    fun shouldShowPermissionRationale(activity: Activity, permission: String): Boolean {
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, permission)
    }

    fun isPermissionGranted(context: Context, permission: String): Boolean {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
    }

    fun getPermissionDescription(permission: String): String {
        return when (permission) {
            Manifest.permission.ACCESS_WIFI_STATE -> "Access WiFi state"
            Manifest.permission.CHANGE_WIFI_STATE -> "Change WiFi state"
            Manifest.permission.ACCESS_FINE_LOCATION -> "Access location for WiFi scanning"
            Manifest.permission.POST_NOTIFICATIONS -> "Show notifications"
            Manifest.permission.FOREGROUND_SERVICE -> "Run background service"
            Manifest.permission.WAKE_LOCK -> "Keep device awake during testing"
            else -> "Required permission"        }
    }
}