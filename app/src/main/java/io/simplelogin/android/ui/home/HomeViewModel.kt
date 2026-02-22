package io.simplelogin.android.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.AliasListManager
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val snackbarManager: SnackbarManager,
    private val aliasListManager: AliasListManager,
    private val copyToClipboard: CopyToClipboardUseCase,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    private val aliasFilterModeFlow = MutableStateFlow(AliasFilterMode.ALL)

    init {
        viewModelScope.launch {
            combine(
                observeSessionSettings(),
                aliasFilterModeFlow
            ) { settings, filterMode ->
                settings to filterMode
            }
                .collect { (settings, filterMode) ->
                    settings.apiKey?.let {
                        aliasListManager.refresh(apiKey = it, filterMode = filterMode)
                            .onFailure(::handle)
                    }
                }
        }
    }

    init {
        viewModelScope.launch {
            aliasListManager.state.collect {
                loadingState.value = it.isModifying
            }
        }
    }

    val stateFlow = combine(
        observeDeviceSettings(),
        aliasFilterModeFlow,
        aliasListManager.state,
    ) { deviceSettings, aliasFilterMode, aliasesListState ->
        HomeScreenState(
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
            snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
        }
    }

    fun updateAliasFilterMode(newMode: AliasFilterMode) {
        viewModelScope.launch {
            if (aliasFilterModeFlow.value != newMode) {
                aliasFilterModeFlow.emit(newMode)
            }
        }
    }

    fun fetchMoreAliases() {
        viewModelScope.launch {
            aliasListManager.fetchMore()
                .onFailure(::handle)
        }
    }

    fun refresh() {
        viewModelScope.launch {
            aliasListManager.refresh()
                .onFailure(::handle)
        }
    }

    fun toggle(alias: Alias) {
        viewModelScope.launch {
            aliasListManager.toggle(aliasId = alias.id)
                .fold(onSuccess = { enabled ->
                    val message = if (enabled.value) {
                        context.getString(R.string.alias_is_enabled, alias.email)
                    } else {
                        context.getString(R.string.alias_is_disabled, alias.email)
                    }
                    val config = SnackbarConfiguration(
                        message = message,
                        type = if (enabled.value) SnackbarType.SUCCESS else SnackbarType.INFORMATION
                    )
                    snackbarManager.showSnackbar(config)
                }, onFailure = ::handle)
        }
    }

    fun pin(alias: Alias) {
        viewModelScope.launch {
            aliasListManager.pin(aliasId = alias.id)
                .fold(onSuccess = {
                    val config = SnackbarConfiguration(
                        message = context.getString(R.string.alias_is_pinned, alias.email),
                        type = SnackbarType.SUCCESS
                    )
                    snackbarManager.showSnackbar(config)
                }, onFailure = ::handle)
        }
    }

    fun unpin(alias: Alias) {
        viewModelScope.launch {
            aliasListManager.unpin(aliasId = alias.id)
                .fold(onSuccess = {
                    val config = SnackbarConfiguration(
                        message = context.getString(R.string.alias_is_unpinned, alias.email),
                        type = SnackbarType.INFORMATION
                    )
                    snackbarManager.showSnackbar(config)
                }, onFailure = ::handle)
        }
    }

    fun delete(alias: Alias) {
        viewModelScope.launch {
            aliasListManager.delete(aliasId = alias.id)
                .fold(onSuccess = {
                    val config = SnackbarConfiguration(
                        message = context.getString(R.string.alias_is_deleted, alias.email)
                    )
                    snackbarManager.showSnackbar(config)
                }, onFailure = ::handle)
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
        snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
    }

    private suspend fun handle(error: ApiError) {
        val config = SnackbarConfiguration(
            message = error.description(context),
            type = SnackbarType.FAILURE
        )
        snackbarManager.showSnackbar(config)
    }
}