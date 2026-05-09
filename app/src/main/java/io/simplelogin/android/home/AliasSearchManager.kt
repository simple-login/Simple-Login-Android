package io.simplelogin.android.home

import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.simplelogin.core.common.PAGE_SIZE
import io.simplelogin.core.common.getAs
import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasId
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.network.EnabledResponse
import io.simplelogin.core.network.datasource.AliasDetailsRemoteDatasource
import io.simplelogin.core.network.datasource.AliasesRemoteDatasource
import io.simplelogin.core.network.datasource.pin
import io.simplelogin.core.network.datasource.unpin
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class AliasSearchState(
    val query: String = "",
    val aliases: List<Alias> = emptyList(),
    val fetchError: ApiError? = null,
    val isRefreshing: Boolean = false,
    val isFetching: Boolean = false,
    val isModifying: Boolean = false
) {
    companion object {
        val Default = AliasSearchState()
    }
}

interface AliasSearchManager {
    val state: Flow<AliasSearchState>
    fun updateQuery(query: String)
    suspend fun refresh(): Result<Unit, ApiError>
    suspend fun fetchMore(): Result<Unit, ApiError>
    suspend fun toggle(aliasId: AliasId): Result<EnabledResponse, ApiError>
    suspend fun pin(aliasId: AliasId): Result<Unit, ApiError>
    suspend fun unpin(aliasId: AliasId): Result<Unit, ApiError>
    suspend fun delete(aliasId: AliasId): Result<Unit, ApiError>
}

@AssistedFactory
interface AliasSearchManagerFactory {
    fun create(apiKeyValue: String): AliasSearchManagerImpl
}

class AliasSearchManagerImpl @AssistedInject constructor(
    @Assisted apiKeyValue: String,
    private val aliasesDatasource: AliasesRemoteDatasource,
    private val aliasDetailsDatasource: AliasDetailsRemoteDatasource
) : AliasSearchManager {
    private val apiKey = ApiKey(value = apiKeyValue)
    private val query = MutableStateFlow("")
    private val aliases = MutableStateFlow<List<Alias>>(emptyList())
    private val fetchError = MutableStateFlow<ApiError?>(null)
    private val isRefreshing = MutableStateFlow(false)
    private val isFetching = MutableStateFlow(false)
    private val isModifying = MutableStateFlow(false)

    override val state = combine(
        listOf(
            query,
            aliases,
            fetchError,
            isRefreshing,
            isFetching,
            isModifying
        )
    ) { values ->
        AliasSearchState(
            query = values.getAs(index = 0) ?: "",
            aliases = values.getAs(index = 1) ?: emptyList(),
            fetchError = values.getAs(index = 2),
            isRefreshing = values.getAs(index = 3, default = false),
            isFetching = values.getAs(index = 4, default = false),
            isModifying = values.getAs(index = 5, default = false)
        )
    }.stateIn(
        scope = CoroutineScope(SupervisorJob() + Dispatchers.Default),
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AliasSearchState.Default
    )

    private var canFetchMore = true
    private var currentPage = 0

    override fun updateQuery(query: String) {
        this.query.value = query
    }

    override suspend fun refresh(): Result<Unit, ApiError> {
        aliases.value = emptyList()
        isFetching.value = false
        isRefreshing.value = false
        isModifying.value = false
        canFetchMore = true
        currentPage = 0
        return fetchMore()
    }

    override suspend fun fetchMore(): Result<Unit, ApiError> {
        if (query.value.isEmpty() || isFetching.value || isRefreshing.value || isModifying.value || !canFetchMore) {
            return Result.Success(Unit)
        }

        fetchError.value = null
        isFetching.value = true
        isRefreshing.value = aliases.value.isEmpty()
        return aliasesDatasource.searchAliases(
            apiKey = apiKey,
            query = query.value,
            pageId = currentPage
        ).fold(onSuccess = { result ->
            val newAliases = result.aliases
            isFetching.value = false
            isRefreshing.value = false
            this.aliases.value += newAliases
            currentPage += 1
            canFetchMore = newAliases.isNotEmpty() && newAliases.size <= PAGE_SIZE
            Result.Success(Unit)
        }, onFailure = {
            fetchError.value = it
            Result.Failure(it)
        })
    }

    override suspend fun toggle(aliasId: AliasId): Result<EnabledResponse, ApiError> {
        isModifying.value = true
        return aliasesDatasource.toggle(apiKey = apiKey, aliasId = aliasId)
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
        isModifying.value = true
        return aliasDetailsDatasource.pin(apiKey = apiKey, aliasId = aliasId)
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
        isModifying.value = true
        return aliasDetailsDatasource.unpin(apiKey = apiKey, aliasId = aliasId)
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
        isModifying.value = true
        return aliasesDatasource.delete(apiKey = apiKey, aliasId = aliasId)
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
}