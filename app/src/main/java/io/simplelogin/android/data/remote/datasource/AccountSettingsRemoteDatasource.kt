package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.core.model.Result
import io.simplelogin.android.core.model.api.ApiError
import io.simplelogin.android.core.model.api.ApiKey
import io.simplelogin.android.core.model.api.Token
import io.simplelogin.android.core.model.api.UpdateUserInfoOption
import io.simplelogin.android.core.model.api.UpdateUserSettingsOption
import io.simplelogin.android.core.model.api.UsableDomain
import io.simplelogin.android.core.model.api.UserInfo
import io.simplelogin.android.core.model.api.UserSettings
import io.simplelogin.android.core.network.ApiService
import io.simplelogin.android.core.network.OkResponse
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