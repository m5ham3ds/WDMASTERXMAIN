package com.wdmaster.app.exception

sealed class AppException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    class NetworkException(message: String = "Network error occurred") : AppException(message)

    class DatabaseException(
        message: String = "Database error occurred",
        cause: Throwable? = null
    ) : AppException(message, cause)

    class PermissionException(message: String = "Permission denied") : AppException(message)

    class ConfigException(message: String = "Invalid configuration") : AppException(message)

    class ServiceException(message: String = "Service error occurred") : AppException(message)

    class ExportException(message: String = "Export failed") : AppException(message)

    class WiFiException(message: String = "WiFi operation failed") : AppException(message)

    companion object {
        fun handle(exception: Throwable): AppException {
            return when (exception) {
                is AppException -> exception
                is java.net.ConnectException -> NetworkException("Connection failed")
                is android.database.sqlite.SQLiteException ->
                    DatabaseException(exception.message ?: "Database error", exception)

                is SecurityException ->
                    PermissionException(exception.message ?: "Permission denied")

                else ->
                    ServiceException(exception.message ?: "Unknown error")
            }
        }
    }
}

// Result wrapper for safe operations
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class Failure(val exception: AppException) : Result<Nothing>()

    companion object {
        fun <T> of(block: () -> T): Result<T> {
            return try {
                Success(block())
            } catch (e: Exception) {
                Failure(AppException.handle(e))
            }
        }
    }
}

// Extension functions
fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
    if (this is Result.Success) action(data)
    return this
}

fun <T> Result<T>.onFailure(action: (AppException) -> Unit): Result<T> {
    if (this is Result.Failure) action(exception)
    return this
}
