package io.simplelogin.android.data.remote.datasource

import io.simplelogin.android.data.models.api.Aliases
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.ApiService
import io.simplelogin.android.data.util.Result
import javax.inject.Inject

interface AliasesRemoteDatasource {
    suspend fun fetchAliases(apiKey: String, filterMode: AliasFilterMode, pageId: Int): Result<Aliases, ApiError>
}

class AliasesRemoteDatasourceImpl @Inject constructor(private val apiService: ApiService) :
    BaseRemoteDatasource(), AliasesRemoteDatasource {
    override suspend fun fetchAliases(
        apiKey: String,
        filterMode: AliasFilterMode,
        pageId: Int
    ): Result<Aliases, ApiError> {
        val params: Map<String?, String>? = when (filterMode) {
            AliasFilterMode.ALL -> null
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
}