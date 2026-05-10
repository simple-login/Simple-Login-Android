package io.simplelogin.core.model.api

sealed class ApiError {
    data class HttpError(val code: Int, val errorMessage: String?) : ApiError()
    data class UnknownError(val e: Exception) : ApiError()
}
