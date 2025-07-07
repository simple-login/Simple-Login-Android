package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.util.Result
import retrofit2.Response

open class BaseRemoteDatasource {
    suspend fun <T> safeApiCall(call: suspend () -> Response<T>) : Result<T, ApiError> {
        return try {
            val result = call.invoke()
            val body = result.body()
            if (body != null) {
                Result.Success(body)
            } else {
                Result.Failure(ApiError.HttpError(result.code()))
            }
        } catch (e: Exception) {
            Result.Failure(ApiError.UnknownError(e))
        }
    }
}