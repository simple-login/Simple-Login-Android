package io.simplelogin.android.ui.home.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.AliasOptions
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailboxes
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
                coroutineScope {
                    val mailboxes = async { datasource.getMailboxes(apiKey = apiKey) }
                    val options = async { datasource.getAliasOptions(apiKey = apiKey) }
                    handleResults(
                        mailboxesResult = mailboxes.await(),
                        optionsResult = options.await()
                    )
                }
            }
        }
    }

    private suspend fun handleResults(
        mailboxesResult: Result<Mailboxes, ApiError>,
        optionsResult: Result<AliasOptions, ApiError>
    ) {
        when {
            mailboxesResult is Result.Success && optionsResult is Result.Success -> {
                val devicePreferences = observeDeviceSettings().first()
                val sortedSuffixes = optionsResult.value.suffixes.sortedWith(
                    compareByDescending<Suffix> { it.isCustom }
                        .thenByDescending { it.isPremium }
                )
                val sortedMailboxes = mailboxesResult.value.value.sortedWith(
                    compareByDescending { it.default }
                )
                _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        defaultPrefix = devicePreferences.defaultPrefix.generate(),
                        aliasOptions = optionsResult.value.copy(suffixes = sortedSuffixes),
                        mailboxes = sortedMailboxes,
                        fetchError = null
                    )
                }
            }

            mailboxesResult is Result.Failure ->
                _stateFlow.update {
                    it.copy(isLoading = false, fetchError = mailboxesResult.error)
                }


            optionsResult is Result.Failure -> _stateFlow.update {
                it.copy(isLoading = false, fetchError = optionsResult.error)
            }
        }
    }
}