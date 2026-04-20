package io.simplelogin.core.network.datasource

import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasOptions
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.Mailboxes
import io.simplelogin.core.network.ApiService
import io.simplelogin.core.network.CreateAliasBody
import javax.inject.Inject

interface CreationRemoteDatasource {
    suspend fun getAliasOptions(apiKey: ApiKey): Result<AliasOptions, ApiError>
    suspend fun getMailboxes(apiKey: ApiKey): Result<Mailboxes, ApiError>
    suspend fun create(apiKey: ApiKey, body: CreateAliasBody): Result<Alias, ApiError>
}

class CreationRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), CreationRemoteDatasource {
    override suspend fun getAliasOptions(apiKey: ApiKey): Result<AliasOptions, ApiError> =
        safeApiCall { apiService.getAliasOptions(apiKey = apiKey) }

    override suspend fun getMailboxes(apiKey: ApiKey): Result<Mailboxes, ApiError> =
        safeApiCall { apiService.getMailboxes(apiKey = apiKey) }

    override suspend fun create(apiKey: ApiKey, body: CreateAliasBody): Result<Alias, ApiError> =
        safeApiCall { apiService.createAlias(apiKey = apiKey, body = body, hostname = null) }
}