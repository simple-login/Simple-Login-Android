package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface CreationRemoteDatasource {
    suspend fun getAliasOptions(apiKey: ApiKey): Result<AliasOptions, ApiError>
}

class CreationRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), CreationRemoteDatasource {
    override suspend fun getAliasOptions(apiKey: ApiKey): Result<AliasOptions, ApiError> =
        safeApiCall { apiService.getAliasOptions(apiKey = apiKey) }
}