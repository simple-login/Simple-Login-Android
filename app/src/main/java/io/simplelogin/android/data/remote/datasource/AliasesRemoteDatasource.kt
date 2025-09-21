package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.Aliases
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.api.UpdateAliasOptions
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.remote.EnabledResponse
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface AliasesRemoteDatasource {
    suspend fun fetchStats(apiKey: ApiKey): Result<Stats, ApiError>
    suspend fun fetchAliases(
        apiKey: ApiKey,
        filterMode: AliasFilterMode,
        pageId: Int
    ): Result<Aliases, ApiError>

    suspend fun toggle(apiKey: ApiKey, aliasId: AliasId): Result<EnabledResponse, ApiError>
    suspend fun pin(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError>
    suspend fun unpin(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError>
    suspend fun delete(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError>
    suspend fun getActivities(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<AliasActivity>, ApiError>
}

class AliasesRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), AliasesRemoteDatasource {
    override suspend fun fetchStats(apiKey: ApiKey): Result<Stats, ApiError> =
        safeApiCall { apiService.getStats(apiKey = apiKey) }

    override suspend fun fetchAliases(
        apiKey: ApiKey,
        filterMode: AliasFilterMode,
        pageId: Int
    ): Result<Aliases, ApiError> {
        val params: Map<String?, String>? = when (filterMode) {
            AliasFilterMode.ALL -> null
            AliasFilterMode.PINNED -> mapOf("pinned" to "true")
            AliasFilterMode.ENABLED -> mapOf("enabled" to "true")
            AliasFilterMode.DISABLED -> mapOf("disabled" to "true")
        }

        return if (params != null) {
            safeApiCall {
                apiService.filterAliases(
                    apiKey = apiKey,
                    pageId = pageId,
                    params = params
                )
            }
        } else {
            safeApiCall {
                apiService.getAliases(apiKey = apiKey, pageId = pageId)
            }
        }
    }

    override suspend fun toggle(
        apiKey: ApiKey,
        aliasId: AliasId
    ): Result<EnabledResponse, ApiError> =
        safeApiCall { apiService.toggleAlias(apiKey = apiKey, aliasId = aliasId) }

    override suspend fun pin(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError> =
        safeApiCall {
            apiService.updateAlias(
                apiKey = apiKey,
                aliasId = aliasId,
                body = UpdateAliasOptions(pinned = true)
            )
        }.mapValue {}

    override suspend fun unpin(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError> =
        safeApiCall {
            apiService.updateAlias(
                apiKey = apiKey,
                aliasId = aliasId,
                body = UpdateAliasOptions(pinned = false)
            )
        }.mapValue {}

    override suspend fun delete(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError> =
        safeApiCall {
            apiService.deleteAlias(apiKey = apiKey, aliasId = aliasId)
        }.mapValue {}

    override suspend fun getActivities(
        apiKey: ApiKey,
        aliasId: AliasId,
        page: Int
    ): Result<List<AliasActivity>, ApiError> =
        safeApiCall {
            apiService.getAliasActivities(apiKey = apiKey, aliasId = aliasId, pageId = page)
        }.mapValue { it.activities }
}