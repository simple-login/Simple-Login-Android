package io.simplelogin.android.domain

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.util.Result
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

data class AliasListState(
    val stats: Stats?,
    val aliases: List<Alias>,
    val isRefreshing: Boolean,
    val isFetching: Boolean
) {
    companion object {
        val Default = AliasListState(
            stats = null,
            aliases = emptyList(),
            isRefreshing = false,
            isFetching = false
        )
    }
}

interface AliasListManager {
    val state: Flow<AliasListState>
    suspend fun refresh(apiKey: String? = null, filterMode: AliasFilterMode? = null): Result<Unit, ApiError>
    suspend fun fetchMore(): Result<Unit, ApiError>
}

class AliasListManagerImpl @Inject constructor(private val datasource: AliasesRemoteDatasource) :
    AliasListManager {
    private val _state = MutableStateFlow<AliasListState>(AliasListState.Default)
    override val state = _state

    private var canFetchMore = true
    private var apiKey: String? = null
    private var currentPage = 0
    private var filterMode: AliasFilterMode? = null

    override suspend fun refresh(apiKey: String?, filterMode: AliasFilterMode?): Result<Unit, ApiError> {
        apiKey?.let { this.apiKey = it }
        filterMode?.let { this.filterMode = it }
        _state.value = AliasListState.Default
        canFetchMore = true
        currentPage = 0
        return fetchMore()
    }

    override suspend fun fetchMore(): Result<Unit, ApiError> {
        if (_state.value.isFetching || _state.value.isRefreshing || !canFetchMore) return Result.Success(Unit)
        val apiKey = apiKey ?: return Result.Success(Unit)
        val filterMode = requireNotNull(filterMode) { "Filter mode is not set" }

        _state.value = _state.value.copy(
            isFetching = true,
            isRefreshing = _state.value.aliases.isEmpty()
        )

        return coroutineScope {
            val statsDeferred = if (_state.value.stats == null) {
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

            _state.value = _state.value.copy(isFetching = false, isRefreshing = false)

            aliasesResult.fold(
                onSuccess = {
                    if (statsResult is Result.Failure) {
                        return@fold statsResult
                    }

                    if (statsResult is Result.Success) {
                        _state.value = _state.value.copy(stats = statsResult.value)
                    }

                    _state.value = _state.value.copy(aliases = _state.value.aliases + it.aliases)

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

    private companion object {
        const val PAGE_SIZE = 20 // Per API set up
    }
}