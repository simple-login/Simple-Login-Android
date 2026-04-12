package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.models.Result
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.UserLogin
import io.simplelogin.android.data.network.ActivateAccountBody
import io.simplelogin.android.data.network.ApiService
import io.simplelogin.android.data.network.EmailBody
import io.simplelogin.android.data.network.LoginBody
import io.simplelogin.android.data.network.MessageResponse
import io.simplelogin.android.data.network.MfaAuthBody
import io.simplelogin.android.data.network.OkResponse
import io.simplelogin.android.data.network.RegisterBody
import javax.inject.Inject

interface LogInSignUpRemoteDatasource {
    suspend fun logIn(
        email: String,
        password: String,
        deviceName: String
    ): Result<UserLogin, ApiError>

    suspend fun mfaAuth(key: String, token: String, deviceName: String): Result<ApiKey, ApiError>
    suspend fun forgotPassword(email: String): Result<OkResponse, ApiError>
    suspend fun signUp(email: String, password: String): Result<MessageResponse, ApiError>
    suspend fun activate(email: String, code: String): Result<MessageResponse, ApiError>
    suspend fun reactivate(email: String): Result<MessageResponse, ApiError>
}

class LogInSignUpRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), LogInSignUpRemoteDatasource {
    override suspend fun logIn(
        email: String,
        password: String,
        deviceName: String
    ): Result<UserLogin, ApiError> =
        safeApiCall {
            apiService.login(
                LoginBody(
                    email = email,
                    password = password,
                    device = deviceName
                )
            )
        }

    override suspend fun mfaAuth(
        key: String,
        token: String,
        deviceName: String
    ): Result<ApiKey, ApiError> =
        safeApiCall {
            apiService.mfaAuth(
                MfaAuthBody(
                    token = token,
                    key = key,
                    device = deviceName
                )
            )
        }

    override suspend fun forgotPassword(email: String): Result<OkResponse, ApiError> =
        safeApiCall { apiService.forgotPassword(EmailBody(email)) }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<MessageResponse, ApiError> =
        safeApiCall { apiService.register(RegisterBody(email = email, password = password)) }

    override suspend fun activate(email: String, code: String): Result<MessageResponse, ApiError> =
        safeApiCall { apiService.activate(ActivateAccountBody(email = email, code = code)) }

    override suspend fun reactivate(email: String): Result<MessageResponse, ApiError> =
        safeApiCall { apiService.reactivate(EmailBody(email)) }
}