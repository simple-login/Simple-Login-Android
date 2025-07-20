package io.simplelogin.android.domain

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.EnabledResponse
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.util.Result
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
import javax.inject.Inject

data class AliasListState(
    val stats: Stats?,
    val aliases: List<Alias>,
    val isRefreshing: Boolean,
    val isFetching: Boolean,
    val isModifying: Boolean
) {
    companion object {
        val Default = AliasListState(
            stats = null,
            aliases = emptyList(),
            isRefreshing = false,
            isFetching = false,
            isModifying = false
        )
    }
}

interface AliasListManager {
    val state: Flow<AliasListState>
    suspend fun refresh(
        apiKey: String? = null,
        filterMode: AliasFilterMode? = null
    ): Result<Unit, ApiError>

    suspend fun fetchMore(): Result<Unit, ApiError>
    suspend fun toggle(aliasId: Int): Result<EnabledResponse, ApiError>
    suspend fun pin(aliasId: Int): Result<Unit, ApiError>
    suspend fun unpin(aliasId: Int): Result<Unit, ApiError>
    suspend fun delete(aliasId: Int): Result<Unit, ApiError>
}

class AliasListManagerImpl @Inject constructor(private val datasource: AliasesRemoteDatasource) :
    AliasListManager {
    private val stats = MutableStateFlow<Stats?>(null)
    private val aliases = MutableStateFlow<List<Alias>>(listOf())
    private val isFetching = MutableStateFlow(false)
    private val isRefreshing = MutableStateFlow(false)
    private val isModifying = MutableStateFlow(false)

    override val state = combine(
        stats,
        aliases,
        isFetching,
        isRefreshing,
        isModifying
    ) { stats, aliases, isFetching, isRefreshing, isModifying ->
        AliasListState(
            stats = stats,
            aliases = aliases,
            isFetching = isFetching,
            isRefreshing = isRefreshing,
            isModifying = isModifying
        )
    }
        .stateIn(
            scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AliasListState.Default
        )

    private var canFetchMore = true
    private var apiKey: String? = null
    private var currentPage = 0
    private var filterMode: AliasFilterMode? = null

    override suspend fun refresh(
        apiKey: String?,
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
                    Result.Failure(it)
                }
            )
        }
    }

    override suspend fun toggle(aliasId: Int): Result<EnabledResponse, ApiError> {
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

    override suspend fun pin(aliasId: Int): Result<Unit, ApiError> {
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

    override suspend fun unpin(aliasId: Int): Result<Unit, ApiError> {
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

    override suspend fun delete(aliasId: Int): Result<Unit, ApiError> {
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

    private companion object {
        const val PAGE_SIZE = 20 // Per API set up
    }
}