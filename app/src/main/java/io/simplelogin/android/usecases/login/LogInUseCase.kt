package io.simplelogin.android.usecases.login

import android.os.Build
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.UserLogin
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

sealed class LogInError {
    data object IncorrectEmailOrPassword: LogInError()
    data class Api(val error: ApiError): LogInError()

    companion object {
        fun fromApiError(error: ApiError): LogInError = when (error) {
            is ApiError.HttpError -> if (error.code == 400) {
                IncorrectEmailOrPassword
            } else {
                Api(error)
            }
            is ApiError.UnknownError -> Api(error)
        }
    }
}

interface LogInUseCase {
    suspend fun invoke(email: String, password: String): Result<UserLogin, LogInError>
}

class LogInUseCaseImpl @Inject constructor(private val datasource: LogInSignUpRemoteDatasource) :
    LogInUseCase {
    override suspend fun invoke(email: String, password: String): Result<UserLogin, LogInError> {
        val deviceName = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
        return datasource.logIn(email = email, password = password, deviceName = deviceName)
            .mapError(LogInError::fromApiError)
    }
}