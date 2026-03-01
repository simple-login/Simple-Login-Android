package io.simplelogin.android.domain

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.EnabledResponse
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.remote.datasource.pin
import io.simplelogin.android.data.remote.datasource.unpin
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.util.getAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class AliasListState(
    val stats: Stats?,
    val aliases: List<Alias>,
    val fetchError: ApiError?,
    val isRefreshing: Boolean,
    val isFetching: Boolean,
    val isModifying: Boolean
) {
    companion object {
        val Default = AliasListState(
            stats = null,
            aliases = emptyList(),
            fetchError = null,
            isRefreshing = false,
            isFetching = false,
            isModifying = false
        )
    }
}

interface AliasListManager {
    val state: Flow<AliasListState>
    suspend fun refresh(
        apiKey: ApiKey? = null,
        filterMode: AliasFilterMode? = null
    ): Result<Unit, ApiError>

    suspend fun fetchMore(): Result<Unit, ApiError>
    suspend fun toggle(aliasId: AliasId): Result<EnabledResponse, ApiError>
    suspend fun pin(aliasId: AliasId): Result<Unit, ApiError>
    suspend fun unpin(aliasId: AliasId): Result<Unit, ApiError>
    suspend fun delete(aliasId: AliasId): Result<Unit, ApiError>
    suspend fun randomAlias(mode: RandomMode, note: String?): Result<Alias, ApiError>
    suspend fun handleNewlyCreatedAlias(alias: Alias)
}

class AliasListManagerImpl @Inject constructor(private val datasource: AliasesRemoteDatasource) :
    AliasListManager {
    private val stats = MutableStateFlow<Stats?>(null)
    private val aliases = MutableStateFlow<List<Alias>>(listOf())
    private val fetchError = MutableStateFlow<ApiError?>(null)
    private val isFetching = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private val isModifying = MutableStateFlow(false)

    override val state = combine(
        listOf(
            stats,
            aliases,
            fetchError,
            isFetching,
            isRefreshing,
            isModifying
        )
    ) { values ->
        AliasListState(
            stats = values.getAs(index = 0),
            aliases = values.getAs(index = 1) ?: listOf(),
            fetchError = values.getAs(index = 2),
            isFetching = values.getAs(index = 3, default = false),
            isRefreshing = values.getAs(index = 4, default = false),
            isModifying = values.getAs(index = 5, default = false),
        )
    }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AliasListState.Default
        )

    private var canFetchMore = true
    private var apiKey: ApiKey? = null
    private var currentPage = 0
    private var filterMode: AliasFilterMode? = null

    override suspend fun refresh(
        apiKey: ApiKey?,
        filterMode: AliasFilterMode?
    ): Result<Unit, ApiError> {
        apiKey?.let { this.apiKey = it }
        filterMode?.let { this.filterMode = it }
        stats.value = null
        aliases.value = listOf()
        isFetching.value = false
        isRefreshing.value = false
        isModifying.value = false
        canFetchMore = true
        currentPage = 0
        return fetchMore()
    }

    override suspend fun fetchMore(): Result<Unit, ApiError> {
        if (isFetching.value || isRefreshing.value || isModifying.value || !canFetchMore) {
            return Result.Success(Unit)
        }
        val apiKey = apiKey ?: return Result.Success(Unit)
        val filterMode = requireNotNull(filterMode) { "Filter mode is not set" }

        fetchError.value = null
        isFetching.value = true
        isRefreshing.value = aliases.value.isEmpty()

        return coroutineScope {
            val statsDeferred = if (stats.value == null) {
                async { datasource.fetchStats(apiKey) }
            } else {
                null
            }

            val aliasesDeferred = async {
                datasource.fetchAliases(
                    apiKey = apiKey,
                    pageId = currentPage,
                    filterMode = filterMode
                )
            }

            val statsResult = statsDeferred?.await()
            val aliasesResult = aliasesDeferred.await()

            isFetching.value = false
            isRefreshing.value = false

            aliasesResult.fold(
                onSuccess = {
                    if (statsResult is Result.Failure) {
                        return@fold statsResult
                    }

                    if (statsResult is Result.Success) {
                        stats.value = statsResult.value
                    }

                    aliases.value = aliases.value + it.aliases

                    currentPage += 1
                    canFetchMore = it.aliases.isNotEmpty() && it.aliases.size <= PAGE_SIZE

                    Result.Success(Unit)
                },
                onFailure = {
                    fetchError.value = it
                    Result.Failure(it)
                }
            )
        }
    }

    override suspend fun toggle(aliasId: AliasId): Result<EnabledResponse, ApiError> {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        isModifying.value = true
        return datasource.toggle(apiKey = apiKey, aliasId = aliasId)
            .fold(onSuccess = { enabled ->
                val aliases = aliases.value.toMutableList()
                val index = aliases.indexOfFirst { it.id == aliasId }

                assert(index != -1) { "Alias with id $aliasId not found" }
                if (index != -1) {
                    aliases[index] = aliases[index].copy(enabled = enabled.value)
                }

                this.aliases.value = aliases
                isModifying.value = false
                Result.Success(enabled)
            }, onFailure = {
                isModifying.value = false
                Result.Failure(it)
            })
    }

    override suspend fun pin(aliasId: AliasId): Result<Unit, ApiError> {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        isModifying.value = true
        return datasource.pin(apiKey = apiKey, aliasId = aliasId)
            .fold(onSuccess = {
                val aliases = aliases.value.toMutableList()
                val index = aliases.indexOfFirst { it.id == aliasId }

                assert(index != -1) { "Alias with id $aliasId not found" }
                if (index != -1) {
                    aliases[index] = aliases[index].copy(pinned = true)
                }

                this.aliases.value = aliases
                isModifying.value = false
                Result.Success(Unit)
            }, onFailure = {
                isModifying.value = false
                Result.Failure(it)
            })
    }

    override suspend fun unpin(aliasId: AliasId): Result<Unit, ApiError> {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        isModifying.value = true
        return datasource.unpin(apiKey = apiKey, aliasId = aliasId)
            .fold(onSuccess = {
                val aliases = aliases.value.toMutableList()
                val index = aliases.indexOfFirst { it.id == aliasId }

                assert(index != -1) { "Alias with id $aliasId not found" }
                if (index != -1) {
                    aliases[index] = aliases[index].copy(pinned = false)
                }

                this.aliases.value = aliases
                isModifying.value = false
                Result.Success(Unit)
            }, onFailure = {
                isModifying.value = false
                Result.Failure(it)
            })
    }

    override suspend fun delete(aliasId: AliasId): Result<Unit, ApiError> {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        isModifying.value = true
        return datasource.delete(apiKey = apiKey, aliasId = aliasId)
            .fold(onSuccess = {
                val aliases = aliases.value.toMutableList()
                aliases.removeAll { it.id == aliasId }

                this.aliases.value = aliases
                isModifying.value = false
                Result.Success(Unit)
            }, onFailure = {
                isModifying.value = false
                Result.Failure(it)
            })
    }

    override suspend fun randomAlias(mode: RandomMode, note: String?): Result<Alias, ApiError> {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        isModifying.value = true
        return datasource.random(apiKey = apiKey, mode = mode, note = note)
            .fold(onSuccess = { randomAlias ->
                if (filterMode == AliasFilterMode.ALL || filterMode == AliasFilterMode.ENABLED) {
                    aliases.update { currentAliases ->
                        listOf(randomAlias) + currentAliases
                    }
                }
                isModifying.value = false
                Result.Success(randomAlias)
            }, onFailure = { error ->
                isModifying.value = false
                Result.Failure(error)
            })
    }

    override suspend fun handleNewlyCreatedAlias(alias: Alias) {
        if (filterMode == AliasFilterMode.ALL || filterMode == AliasFilterMode.ENABLED) {
            aliases.update { currentAliases ->
                listOf(alias) + currentAliases
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20 // Per API set up
    }
}