package com.wdmaster.app.data.repository

import com.wdmaster.app.exception.AppException
import com.wdmaster.app.exception.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

abstract class BaseRepository {
    
    protected suspend fun <T> safeApiCall(apiCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Success(apiCall())
            } catch (e: Exception) {
                Result.Failure(AppException.handle(e))
            }
        }
    }
    
    protected suspend fun <T> safeDbCall(dbCall: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.Success(dbCall())
            } catch (e: Exception) {
                Result.Failure(AppException.DatabaseException(e.message, e))
            }
        }
    }
    
    protected fun <T> Result<T>.onSuccess(action: (T) -> Unit): Result<T> {
        if (this is Result.Success) action(data)
        return this
    }
    
    protected fun <T> Result<T>.onFailure(action: (AppException) -> Unit): Result<T> {
        if (this is Result.Failure) action(exception)
        return this
    }
    
    protected suspend fun <T> Result<T>.map(transform: suspend (T) -> T): Result<T> {
        return when (this) {
            is Result.Success -> Result.Success(transform(data))
            is Result.Failure -> this
        }
    }
}