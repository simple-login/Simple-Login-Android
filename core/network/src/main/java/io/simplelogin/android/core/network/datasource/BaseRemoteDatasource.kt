package io.simplelogin.android.core.network.datasource

import com.google.gson.Gson
import io.simplelogin.android.core.model.Result
import io.simplelogin.android.core.model.api.ApiError
import retrofit2.Response

open class BaseRemoteDatasource {
    suspend fun <T> safeApiCall(call: suspend () -> Response<T>): Result<T, ApiError> {
        return try {
            val result = call.invoke()
            val body = result.body()
            if (body != null) {
                Result.Success(body)
            } else {
                val errorMessage = result.errorBody()?.string()?.let {
                    Gson().fromJson(it, ErrorResponse::class.java).error
                }
                Result.Failure(
                    ApiError.HttpError(
                        code = result.code(),
                        errorMessage = errorMessage
                    )
                )
            }
        } catch (e: Exception) {
            Result.Failure(ApiError.UnknownError(e))
        }
    }
}

private data class ErrorResponse(val error: String)