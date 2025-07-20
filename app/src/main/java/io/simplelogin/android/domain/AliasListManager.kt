package io.simplelogin.android.domain

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import io.simplelogin.android.data.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface AliasListManager {
    val aliases: Flow<List<Alias>>
    val isFetching: Flow<Boolean>

    suspend fun refresh(apiKey: String, filterMode: AliasFilterMode): Result<Unit, ApiError>
    suspend fun fetchMore(): Result<Unit, ApiError>
}

class AliasListManagerImpl @Inject constructor(private val datasource: AliasesRemoteDatasource) :
    AliasListManager {
    private val _aliases = MutableStateFlow<List<Alias>>(emptyList())
    override val aliases = _aliases

    private val _isFetching = MutableStateFlow(false)
    override val isFetching = _isFetching

    private var canFetchMore = true
    private var apiKey: String? = null
    private var currentPage = 0
    private var filterMode: AliasFilterMode? = null

    override suspend fun refresh(apiKey: String, filterMode: AliasFilterMode): Result<Unit, ApiError> {
        this.apiKey = apiKey
        this.filterMode = filterMode
        _aliases.value = listOf()
        canFetchMore = true
        currentPage = 0
        return fetchMore()
    }

    override suspend fun fetchMore(): Result<Unit, ApiError> {
        if (_isFetching.value || !canFetchMore) return Result.Success(Unit)
        val apiKey = apiKey ?: return Result.Success(Unit)
        val filterMode = requireNotNull(filterMode) { "Filter mode is not set" }
        _isFetching.value = true

        return datasource.fetchAliases(
            apiKey = apiKey,
            pageId = currentPage,
            filterMode = filterMode
        ).fold(onSuccess = {
            _aliases.value = _aliases.value + it.aliases
            currentPage += 1
            canFetchMore = it.aliases.isNotEmpty() && it.aliases.size <= PAGE_SIZE
            _isFetching.value = false
            Result.Success(Unit)
        }, onFailure = {
            _isFetching.value = false
            Result.Failure(it)
        })
    }

    private companion object {
        const val PAGE_SIZE = 20 // Per API set up
    }
}