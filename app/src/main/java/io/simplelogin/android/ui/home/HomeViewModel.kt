package io.simplelogin.android.ui.home

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.ui.AliasFilterMode
import io.simplelogin.android.domain.AliasListManager
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.usecases.CopyToClipboardUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val snackbarManager: SnackbarManager,
    private val aliasListManager: AliasListManager,
    private val copyToClipboardUseCase: CopyToClipboardUseCase,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    observeDeviceSettingsUseCase: ObserveDeviceSettingsUseCase
) : ViewModel() {
    private val aliasFilterModeFlow = MutableStateFlow<AliasFilterMode>(AliasFilterMode.ALL)

    val stateFlow = combine(
        observeDeviceSettingsUseCase(),
        aliasFilterModeFlow,
        aliasListManager.aliases,
        aliasListManager.isFetching
    ) { deviceSettings, aliasFilterMode, aliases, isFetching ->
        HomeScreenState(
            deviceSettings = deviceSettings,
            aliasFilterMode = aliasFilterMode,
            aliases = aliases,
            isFetching = isFetching
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = HomeScreenState.Default
    )

    fun setUpAliasListManager() {
        viewModelScope.launch {
            observeSessionSettings.invoke().collect {
                if (it.apiKey != null) {
                    aliasListManager.setApiKey(it.apiKey)
                    aliasListManager.setFilterModeAndRefresh(aliasFilterModeFlow.value)
                        .fold(onSuccess = {}, onFailure = ::handle)
                }
            }
        }
    }

    fun copyAliasAddress(email: String) {
        viewModelScope.launch {
            copyToClipboardUseCase.invoke(
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
                aliasListManager.setFilterModeAndRefresh(newMode)
                    .fold(onSuccess = {}, onFailure = ::handle)
            }
        }
    }

    fun fetchMoreAliases() {
        viewModelScope.launch {
            aliasListManager.fetchMore()
        }
    }

    private fun handle(error: ApiError) {
        print(error)
    }
}