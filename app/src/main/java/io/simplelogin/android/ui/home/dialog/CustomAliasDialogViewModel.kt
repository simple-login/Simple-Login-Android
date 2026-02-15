package io.simplelogin.android.ui.home.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CustomAliasDialogViewModel @Inject constructor(
    private val datasource: CreationRemoteDatasource,
    private val observeDeviceSettings: ObserveDeviceSettingsUseCase,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(CustomAliasDialogState.Default)
    val stateFlow: StateFlow<CustomAliasDialogState> = _stateFlow.asStateFlow()

    init {
        viewModelScope.launch {
            fetchOptions()
        }
    }

    suspend fun fetchOptions() {
        observeSessionSettings().collect { settings ->
            settings.apiKey?.let { apiKey ->
                when (val options = datasource.getAliasOptions(apiKey = apiKey)) {
                    is Result.Success -> _stateFlow.update { state ->
                        val devicePreferences = observeDeviceSettings().first()
                        val sortedSuffixes = options.value.suffixes.sortedWith(
                            compareByDescending<Suffix> { it.isCustom }
                                .thenByDescending { it.isPremium }
                        )
                        state.copy(
                            isLoading = false,
                            defaultPrefix = devicePreferences.defaultPrefix.generate(),
                            aliasOptions = options.value.copy(suffixes = sortedSuffixes),
                            fetchError = null
                        )
                    }

                    is Result.Failure -> _stateFlow.update { state ->
                        state.copy(
                            isLoading = false,
                            defaultPrefix = null,
                            aliasOptions = null,
                            fetchError = options.error
                        )
                    }
                }
            }
        }
    }
}