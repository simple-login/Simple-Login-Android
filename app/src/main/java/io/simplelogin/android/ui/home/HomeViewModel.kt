package io.simplelogin.android.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.AliasListManagerFactory
import io.simplelogin.android.domain.AliasSearchManagerFactory
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.ShowSnackbarFailureUseCase
import io.simplelogin.android.usecases.ShowSnackbarInformationUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = HomeViewModel.Factory::class)
class HomeViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @LoadingState private val loadingState: LoadingStateFlow,
    @Assisted private val apiKeyValue: String,
    aliasListManagerFactory: AliasListManagerFactory,
    aliasSearchManagerFactory: AliasSearchManagerFactory,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val showSnackbarInformation: ShowSnackbarInformationUseCase,
    private val showSnackbarFailure: ShowSnackbarFailureUseCase,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(apiKeyValue: String): HomeViewModel
    }

    private val aliasListManager = aliasListManagerFactory.create(apiKeyValue)
    private val aliasSearchManager = aliasSearchManagerFactory.create(apiKeyValue)
    val searchStateFlow = aliasSearchManager.state

    private val aliasFilterModeFlow = MutableStateFlow(AliasFilterMode.ALL)

    init {
        viewModelScope.launch {
            aliasFilterModeFlow.collect { filterMode ->
                aliasListManager.refresh(filterMode = filterMode)
                    .onFailure(::handle)
            }

            aliasListManager.state.collect {
                loadingState.value = it.isModifying
            }
        }
    }

    val stateFlow = combine(
        observeSessionSettings(),
        observeDeviceSettings(),
        aliasFilterModeFlow,
        aliasListManager.state,
    ) { session, deviceSettings, aliasFilterMode, aliasesListState ->
        HomeScreenState(
            userInfo = session.userInfo,
            deviceSettings = deviceSettings,
            aliasFilterMode = aliasFilterMode,
            stats = aliasesListState.stats,
            aliases = aliasesListState.aliases,
            fetchError = aliasesListState.fetchError,
            isFetching = aliasesListState.isFetching,
            isRefreshing = aliasesListState.isRefreshing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenState.Default
    )

    fun copyAliasAddress(email: String) {
        viewModelScope.launch {
            copyToClipboard(
                label = context.getString(R.string.alias_address_label),
                content = email
            )
            val message = context.getString(R.string.alias_address_copied, email)
            showSnackbarInformation(message)
        }
    }

    fun updateAliasFilterMode(newMode: AliasFilterMode) {
        viewModelScope.launch {
            if (aliasFilterModeFlow.value != newMode) {
                aliasFilterModeFlow.value = newMode
            }
        }
    }

    fun updateSearchQuery(query: String) {
        aliasSearchManager.updateQuery(query = query)
    }

    fun fetchMoreAliases(isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.fetchMore()
                    .onFailure(::handle)
            } else {
                aliasListManager.fetchMore()
                    .onFailure(::handle)
            }
        }
    }

    fun refresh(isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.refresh()
                    .onFailure(::handle)
            } else {
                aliasListManager.refresh()
                    .onFailure(::handle)
            }
        }
    }

    fun toggle(alias: Alias, isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.toggle(aliasId = alias.id)
                    .fold(onSuccess = { enabled ->
                        val message = if (enabled.value) {
                            context.getString(R.string.alias_is_enabled, alias.email)
                        } else {
                            context.getString(R.string.alias_is_disabled, alias.email)
                        }
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            } else {
                aliasListManager.toggle(aliasId = alias.id)
                    .fold(onSuccess = { enabled ->
                        val message = if (enabled.value) {
                            context.getString(R.string.alias_is_enabled, alias.email)
                        } else {
                            context.getString(R.string.alias_is_disabled, alias.email)
                        }
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            }
        }
    }

    fun pin(alias: Alias, isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.pin(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_pinned, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            } else {
                aliasListManager.pin(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_pinned, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            }
        }
    }

    fun unpin(alias: Alias, isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.unpin(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_unpinned, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            } else {
                aliasListManager.unpin(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_unpinned, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            }
        }
    }

    fun delete(alias: Alias, isSearching: Boolean) {
        viewModelScope.launch {
            if (isSearching) {
                aliasSearchManager.delete(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_deleted, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            } else {
                aliasListManager.delete(aliasId = alias.id)
                    .fold(onSuccess = {
                        val message = context.getString(R.string.alias_is_deleted, alias.email)
                        showSnackbarInformation(message)
                    }, onFailure = ::handle)
            }
        }
    }

    fun randomAlias(mode: RandomMode, note: String?) {
        viewModelScope.launch {
            aliasListManager.randomAlias(mode = mode, note = note)
                .fold(onSuccess = {
                    copyAndShowSnackbar(it)
                }, onFailure = ::handle)
        }
    }

    fun handleCreatedAlias(alias: Alias) {
        viewModelScope.launch {
            aliasListManager.handleNewlyCreatedAlias(alias)
            copyAndShowSnackbar(alias)
        }
    }

    private suspend fun copyAndShowSnackbar(alias: Alias) {
        val settings = observeDeviceSettings().first()
        val copyAfterCreating = settings.copyAfterCreating
        if (copyAfterCreating) {
            copyToClipboard(
                label = context.getString(R.string.alias_address_label),
                content = alias.email
            )
        }
        val message = context.getString(
            if (copyAfterCreating) R.string.alias_created_and_copied_to_clipboard else R.string.alias_created,
            alias.email
        )
        showSnackbarInformation(message)
    }

    private suspend fun handle(error: ApiError) {
        showSnackbarFailure(error.description(context))
    }
}