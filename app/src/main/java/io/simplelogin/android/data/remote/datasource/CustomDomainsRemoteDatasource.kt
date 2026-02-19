package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.CustomDomains
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface CustomDomainsRemoteDatasource {
    suspend fun getCustomDomains(apiKey: ApiKey): Result<CustomDomains, ApiError>
}

class CustomDomainsRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), CustomDomainsRemoteDatasource {
    override suspend fun getCustomDomains(apiKey: ApiKey): Result<CustomDomains, ApiError> =
        safeApiCall { apiService.getCustomDomains(apiKey = apiKey) }
}