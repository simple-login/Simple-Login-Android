package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.CustomDomain
import io.simplelogin.android.models.api.CustomDomains
import io.simplelogin.android.models.api.DeletedAlias
import io.simplelogin.android.models.api.UpdateCustomDomainOption
import javax.inject.Inject

interface CustomDomainsRemoteDatasource {
    suspend fun getCustomDomains(apiKey: ApiKey): Result<CustomDomains, ApiError>
    suspend fun updateCustomDomains(
        apiKey: ApiKey,
        domain: CustomDomain,
        option: UpdateCustomDomainOption
    ): Result<CustomDomain, ApiError>

    suspend fun getDeletedAliases(
        apiKey: ApiKey,
        domain: CustomDomain
    ): Result<List<DeletedAlias>, ApiError>
}

class CustomDomainsRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), CustomDomainsRemoteDatasource {
    override suspend fun getCustomDomains(apiKey: ApiKey): Result<CustomDomains, ApiError> =
        safeApiCall { apiService.getCustomDomains(apiKey = apiKey) }

    override suspend fun updateCustomDomains(
        apiKey: ApiKey,
        domain: CustomDomain,
        option: UpdateCustomDomainOption
    ): Result<CustomDomain, ApiError> =
        safeApiCall {
            apiService.updateCustomDomain(
                apiKey = apiKey,
                domainId = domain.id,
                body = option
            )
        }.mapValue { it.customDomain }

    override suspend fun getDeletedAliases(
        apiKey: ApiKey,
        domain: CustomDomain
    ): Result<List<DeletedAlias>, ApiError> =
        safeApiCall {
            apiService.getDeletedAliases(apiKey = apiKey, domainId = domain.id)
        }.mapValue { it.value }
}