package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.models.Result
import io.simplelogin.android.models.api.Alias
import io.simplelogin.android.models.api.AliasId
import io.simplelogin.android.models.api.Aliases
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.models.api.RandomMode
import io.simplelogin.android.models.api.Stats
import io.simplelogin.android.data.network.ApiService
import io.simplelogin.android.data.network.EnabledResponse
import io.simplelogin.android.data.network.NoteBody
import io.simplelogin.android.data.network.SearchBody
import javax.inject.Inject

interface AliasesRemoteDatasource {
    suspend fun fetchStats(apiKey: ApiKey): Result<Stats, ApiError>
    suspend fun fetchAliases(
        apiKey: ApiKey,
        filterMode: AliasFilterMode,
        pageId: Int
    ): Result<Aliases, ApiError>

    suspend fun searchAliases(
        apiKey: ApiKey,
        query: String,
        pageId: Int
    ): Result<Aliases, ApiError>

    suspend fun toggle(apiKey: ApiKey, aliasId: AliasId): Result<EnabledResponse, ApiError>

    suspend fun delete(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError>

    suspend fun random(apiKey: ApiKey, mode: RandomMode, note: String?): Result<Alias, ApiError>
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

    override suspend fun searchAliases(
        apiKey: ApiKey,
        query: String,
        pageId: Int
    ): Result<Aliases, ApiError> =
        safeApiCall {
            apiService.searchAliases(
                apiKey = apiKey,
                pageId = pageId,
                searchBody = SearchBody(query = query)
            )
        }

    override suspend fun toggle(
        apiKey: ApiKey,
        aliasId: AliasId
    ): Result<EnabledResponse, ApiError> =
        safeApiCall { apiService.toggleAlias(apiKey = apiKey, aliasId = aliasId) }

    override suspend fun delete(apiKey: ApiKey, aliasId: AliasId): Result<Unit, ApiError> =
        safeApiCall {
            apiService.deleteAlias(apiKey = apiKey, aliasId = aliasId)
        }.mapValue {}


    override suspend fun random(
        apiKey: ApiKey,
        mode: RandomMode,
        note: String?
    ): Result<Alias, ApiError> =
        safeApiCall {
            apiService.randomAlias(
                apiKey = apiKey,
                mode = mode.value,
                hostname = null,
                body = NoteBody(note)
            )
        }
}