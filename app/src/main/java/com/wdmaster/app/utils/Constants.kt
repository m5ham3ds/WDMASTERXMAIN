package com.wdmaster.app.utils

object Constants {
    
    // Application
    const val APP_NAME = "WiFi Card Master Pro"
    const val APP_VERSION = "1.0.0"
    const val APP_PACKAGE = "com.wdmaster.app"
    
    // Database
    const val DB_NAME = "wdmaster_database"
    const val DB_VERSION = 1
    
    // Preferences
    const val PREFS_NAME = "wdmaster_settings"
    const val PREF_DARK_MODE = "dark_mode"
    const val PREF_FIRST_LAUNCH = "first_launch"
    const val PREF_LAST_UPDATE = "last_update"
    
    // Notification
    const val NOTIFICATION_CHANNEL_ID = "wdmaster_foreground"
    const val NOTIFICATION_CHANNEL_NAME = "WDMASTER Service"
    const val NOTIFICATION_ID = 2001
    const val NOTIFICATION_REQUEST_CODE = 1001
    
    // Service
    const val SERVICE_ACTION_START = "com.wdmaster.app.action.START"
    const val SERVICE_ACTION_PAUSE = "com.wdmaster.app.action.PAUSE"
    const val SERVICE_ACTION_RESUME = "com.wdmaster.app.action.RESUME"
    const val SERVICE_ACTION_STOP = "com.wdmaster.app.action.STOP"
    const val SERVICE_WAKE_LOCK_TAG = "WDMASTER::TestServiceLock"
    
    // Card Generation
    const val DEFAULT_CARD_LENGTH = 16
    const val MIN_CARD_LENGTH = 8
    const val MAX_CARD_LENGTH = 64
    const val DEFAULT_ALLOWED_CHARS = "0123456789ABCDEF"
    const val DEFAULT_MAX_TRIES = 1000L
    const val MIN_MAX_TRIES = 1L
    const val MAX_MAX_TRIES = 1000000L
    
    // Testing
    const val DEFAULT_DELAY_MS = 500L
    const val MIN_DELAY_MS = 100L
    const val MAX_DELAY_MS = 5000L
    const val DEFAULT_RETRY_COUNT = 3
    const val MIN_RETRY_COUNT = 1
    const val MAX_RETRY_COUNT = 10
    const val TEST_TIMEOUT_MS = 5000L
    
    // Export
    const val EXPORT_DIR = "exports"
    const val EXPORT_FILE_PREFIX = "wdmaster_export"
    const val EXPORT_MAX_AGE_DAYS = 7
    
    // Permissions
    const val PERMISSION_REQUEST_CODE = 1001
    const val NOTIFICATION_PERMISSION_CODE = 1002
    const val LOCATION_PERMISSION_CODE = 1003
    const val STORAGE_PERMISSION_CODE = 1004
    
    // UI
    const val ANIMATION_DURATION = 300
    const val SCROLL_DELAY_MS = 100L
    const val LOG_BATCH_SIZE = 10
    const val LOG_MAX_COUNT = 1000
    const val RESULTS_MAX_COUNT = 100
    
    // Time
    const val MS_PER_SECOND = 1000L
    const val MS_PER_MINUTE = 60000L
    const val MS_PER_HOUR = 3600000L
    const val MS_PER_DAY = 86400000L
    const val SECONDS_PER_MINUTE = 60
    const val SECONDS_PER_HOUR = 3600
    const val SECONDS_PER_DAY = 86400
    
    // Network
    const val WIFI_SIGNAL_EXCELLENT = -50
    const val WIFI_SIGNAL_GOOD = -60
    const val WIFI_SIGNAL_FAIR = -70
    const val WIFI_SIGNAL_WEAK = -80
    const val WIFI_SIGNAL_POOR = -90
    
    // Patterns
    const val PATTERN_WILDCARD = "*"
    const val PATTERN_WILDCARD_ALT = "X"
    const val PATTERN_CLEANUP_DAYS = 30
    
    // Analytics
    const val ANALYTICS_EVENT_START = "test_start"
    const val ANALYTICS_EVENT_STOP = "test_stop"
    const val ANALYTICS_EVENT_SUCCESS = "test_success"
    const val ANALYTICS_EVENT_EXPORT = "test_export"
}