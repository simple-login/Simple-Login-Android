package io.simplelogin.android.models.api

sealed class ApiError {
    data class HttpError(val code: Int, val errorMessage: String?) : ApiError()
    data class UnknownError(val e: Exception) : ApiError()
}