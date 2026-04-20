package io.simplelogin.feature.auth.usecase

import io.simplelogin.core.common.di.DeviceName
import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.UserLogin
import io.simplelogin.core.network.datasource.LogInSignUpRemoteDatasource
import javax.inject.Inject

sealed class LogInError {
    data object IncorrectEmailOrPassword : LogInError()
    data object AccountNotActivated : LogInError()
    data class Api(val error: ApiError) : LogInError()

    companion object {
        fun fromApiError(error: ApiError): LogInError = when (error) {
            is ApiError.HttpError ->
                when (error.code) {
                    400 -> IncorrectEmailOrPassword
                    422 -> AccountNotActivated
                    else -> Api(error)
                }

            is ApiError.UnknownError -> Api(error)
        }
    }
}

interface LogInUseCase {
    suspend operator fun invoke(email: String, password: String): Result<UserLogin, LogInError>
}

class LogInUseCaseImpl @Inject constructor(
    private val datasource: LogInSignUpRemoteDatasource,
    @DeviceName private val deviceName: String
) :
    LogInUseCase {
    override suspend operator fun invoke(
        email: String,
        password: String
    ): Result<UserLogin, LogInError> =
        datasource.logIn(email = email, password = password, deviceName = deviceName)
            .mapError(LogInError::fromApiError)
}