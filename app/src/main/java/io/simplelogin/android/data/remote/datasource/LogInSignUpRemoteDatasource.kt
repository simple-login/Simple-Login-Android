package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.UserLogin
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.remote.EmailBody
import io.simplelogin.android.data.remote.LoginBody
import io.simplelogin.android.data.remote.OkResponse
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface LogInSignUpRemoteDatasource {
    suspend fun logIn(email: String, password: String, deviceName: String): Result<UserLogin, ApiError>
    suspend fun forgotPassword(email: String): Result<OkResponse, ApiError>
}

class LogInSignUpRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), LogInSignUpRemoteDatasource {
    override suspend fun logIn(
        email: String,
        password: String,
        deviceName: String
    ): Result<UserLogin, ApiError> {
        val body = LoginBody(
            email = email,
            password = password,
            device = deviceName
        )
        return safeApiCall { apiService.login(body) }
    }

    override suspend fun forgotPassword(email: String): Result<OkResponse, ApiError> {
        val body = EmailBody(email)
        return safeApiCall { apiService.forgotPassword(body) }
    }
}