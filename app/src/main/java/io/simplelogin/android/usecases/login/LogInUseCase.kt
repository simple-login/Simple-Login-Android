package io.simplelogin.android.usecases.login

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.UserLogin
import io.simplelogin.android.data.remote.datasource.LogInSignUpRemoteDatasource
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

sealed class LogInError {
    data object IncorrectEmailOrPassword: LogInError()
    data class Api(val error: ApiError): LogInError()
}

interface LogInUseCase {
    suspend fun invoke(
        email: String,
        password: String,
        deviceName: String
    ): Result<UserLogin, LogInError>
}

class LogInUseCaseImpl @Inject constructor(val datasource: LogInSignUpRemoteDatasource) :
    LogInUseCase {
    override suspend fun invoke(
        email: String,
        password: String,
        deviceName: String
    ): Result<UserLogin, LogInError> =
        datasource.logIn(email = email, password = password, deviceName = deviceName)
            .mapError { it.toLoginError() }

    private fun ApiError.toLoginError(): LogInError = when (this) {
        is ApiError.HttpError -> if (code == 400) {
            LogInError.IncorrectEmailOrPassword
        } else {
            LogInError.Api(this)
        }

        is ApiError.UnknownError -> LogInError.Api(this)
    }
}