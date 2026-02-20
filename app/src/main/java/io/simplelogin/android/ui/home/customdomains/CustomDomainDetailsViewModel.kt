package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.data.models.api.UpdateCustomDomainOptions
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomDomainDetailsViewModel.Factory::class)
class CustomDomainDetailsViewModel @AssistedInject constructor(
    @Assisted private val domain: CustomDomain,
    private val datasource: CustomDomainsRemoteDatasource,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(domain: CustomDomain): CustomDomainDetailsViewModel
    }

    private val _stateFlow = MutableStateFlow(
        CustomDomainDetailsState(
            domain = domain,
            isUpdating = false,
            updateError = null
        )
    )
    val stateFlow: StateFlow<CustomDomainDetailsState> = _stateFlow

    fun updateDisplayName(displayName: String) {
        updateDomain(UpdateCustomDomainOptions(name = displayName))
    }

    fun updateCatchAll(catchAll: Boolean) {
        updateDomain(UpdateCustomDomainOptions(catchAll = catchAll))
    }

    fun updateRandomPrefixGeneration(random: Boolean) {
        updateDomain(UpdateCustomDomainOptions(randomPrefixGeneration = random))
    }

    fun clearUpdateError() {
        _stateFlow.update { it.copy(updateError = null) }
    }

    private fun updateDomain(options: UpdateCustomDomainOptions) {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isUpdating = true) }
            val result = datasource.updateCustomDomains(
                apiKey = apiKey,
                domain = stateFlow.value.domain,
                options = options
            )
            when (result) {
                is Result.Success -> _stateFlow.update {
                    it.copy(
                        domain = result.value,
                        isUpdating = false
                    )
                }

                is Result.Failure -> _stateFlow.update {
                    it.copy(isUpdating = false, updateError = result.error)
                }
            }
        }
    }

    private fun withApiKey(perform: suspend (ApiKey) -> Unit) {
        viewModelScope.launch {
            observeSessionSettings().collect { settings ->
                settings.apiKey?.let {
                    perform(it)
                }
            }
        }
    }
}