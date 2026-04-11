package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.Token
import io.simplelogin.android.data.models.api.UpdateUserInfoOption
import io.simplelogin.android.data.models.api.UpdateUserSettingsOption
import io.simplelogin.android.data.models.api.UsableDomain
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.api.UserSettings
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.remote.OkResponse
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.ApiKey
import javax.inject.Inject

interface AccountSettingsRemoteDatasource {
    suspend fun getUserInfo(apiKey: ApiKey): Result<UserInfo, ApiError>
    suspend fun getUserSettings(apiKey: ApiKey): Result<UserSettings, ApiError>
    suspend fun getUsableDomains(apiKey: ApiKey): Result<List<UsableDomain>, ApiError>
    suspend fun updateUserSettings(
        apiKey: ApiKey,
        options: UpdateUserSettingsOption
    ): Result<UserSettings, ApiError>

    suspend fun updateUserInfo(
        apiKey: ApiKey,
        option: UpdateUserInfoOption
    ): Result<UserInfo, ApiError>

    suspend fun getTemporaryToken(apiKey: ApiKey): Result<Token, ApiError>
    suspend fun unlinkProton(apiKey: ApiKey): Result<OkResponse, ApiError>
}

class AccountSettingsRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(),
    AccountSettingsRemoteDatasource {
    override suspend fun getUserInfo(apiKey: ApiKey): Result<UserInfo, ApiError> =
        safeApiCall { apiService.getUserInfo(apiKey = apiKey) }

    override suspend fun getUserSettings(apiKey: ApiKey): Result<UserSettings, ApiError> =
        safeApiCall { apiService.getUserSettings(apiKey = apiKey) }

    override suspend fun getUsableDomains(apiKey: ApiKey): Result<List<UsableDomain>, ApiError> =
        safeApiCall { apiService.getUsableDomains(apiKey = apiKey) }

    override suspend fun updateUserSettings(
        apiKey: ApiKey,
        options: UpdateUserSettingsOption
    ): Result<UserSettings, ApiError> =
        safeApiCall { apiService.updateUserSettings(apiKey = apiKey, body = options) }

    override suspend fun updateUserInfo(
        apiKey: ApiKey,
        option: UpdateUserInfoOption
    ): Result<UserInfo, ApiError> =
        safeApiCall { apiService.updateUserInfo(apiKey = apiKey, body = option) }

    override suspend fun getTemporaryToken(apiKey: ApiKey): Result<Token, ApiError> =
        safeApiCall { apiService.getCookieToken(apiKey = apiKey) }

    override suspend fun unlinkProton(apiKey: ApiKey): Result<OkResponse, ApiError> =
        safeApiCall { apiService.unlinkProton(apiKey = apiKey) }
}