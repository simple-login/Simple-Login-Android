package io.simplelogin.android.domain

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.data.remote.datasource.AliasesRemoteDatasource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

interface AliasListManager {
    val aliases: Flow<List<Alias>>
    val isFetching: Flow<Boolean>
    val canFetchMore: Boolean

    fun setApiKey(apiKey: String)
    fun setFilterModeAndRefresh(filterMode: AliasFilterMode)
    suspend fun fetchMore(): ApiError?
}

class AliasListManagerImpl @Inject constructor(private val datasource: AliasesRemoteDatasource) :
    AliasListManager {
    private val _aliases = MutableStateFlow<List<Alias>>(emptyList())
    override val aliases = _aliases

    private val _isFetching = MutableStateFlow(false)
    override val isFetching = _isFetching

    private var _canFetchMore = true
    override val canFetchMore = _canFetchMore

    private var apiKey: String? = null
    private var currentPage = 0
    private var filterMode: AliasFilterMode? = null

    override fun setApiKey(apiKey: String) {
        this.apiKey = apiKey
    }

    override fun setFilterModeAndRefresh(filterMode: AliasFilterMode) {
        this.filterMode = filterMode
        _aliases.value = listOf()
        _canFetchMore = true
        currentPage = 0
    }

    override suspend fun fetchMore(): ApiError? {
        val apiKey = requireNotNull(apiKey) { "API key is not set" }
        val filterMode = requireNotNull(filterMode) { "Filter mode is not set" }
        _isFetching.value = true

        val result = datasource.filterAliases(
            apiKey = apiKey,
            pageId = currentPage,
            filterMode = filterMode
        ).fold(onSuccess = {
            _aliases.value = _aliases.value + it.aliases
            currentPage += 1
            _canFetchMore = it.aliases.size <= PAGE_SIZE
            null
        }, onFailure = {
            it
        })

        _isFetching.value = false
        return result
    }

    private companion object {
        const val PAGE_SIZE = 20 // Per API set up
    }
}