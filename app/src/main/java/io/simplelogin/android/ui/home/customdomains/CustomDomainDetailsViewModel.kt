package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.CustomDomain
import io.simplelogin.core.model.api.UpdateCustomDomainOption
import io.simplelogin.core.network.datasource.CustomDomainsRemoteDatasource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomDomainDetailsViewModel.Factory::class)
class CustomDomainDetailsViewModel @AssistedInject constructor(
    @Assisted private val domain: CustomDomain,
    @Assisted private val apiKeyValue: String,
    private val datasource: CustomDomainsRemoteDatasource
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(domain: CustomDomain, apiKeyValue: String): CustomDomainDetailsViewModel
    }

    private val _stateFlow = MutableStateFlow(
        CustomDomainDetailsState(
            domain = domain,
            isUpdating = false,
            updateError = null,
            isUpdated = false
        )
    )
    val stateFlow: StateFlow<CustomDomainDetailsState> = _stateFlow

    fun updateDisplayName(displayName: String) {
        updateDomain(UpdateCustomDomainOption.Name(displayName))
    }

    fun updateCatchAll(catchAll: Boolean) {
        updateDomain(UpdateCustomDomainOption.CatchAll(catchAll))
    }

    fun updateRandomPrefixGeneration(random: Boolean) {
        updateDomain(UpdateCustomDomainOption.RandomPrefixGeneration(random))
    }

    fun clearUpdateError() {
        _stateFlow.update { it.copy(updateError = null) }
    }

    fun clearIsUpdated() {
        _stateFlow.update { it.copy(isUpdated = false) }
    }

    private fun updateDomain(option: UpdateCustomDomainOption) {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isUpdating = true) }
            datasource.updateCustomDomains(
                apiKey = apiKey,
                domain = stateFlow.value.domain,
                option = option
            ).fold(onSuccess = { result ->
                _stateFlow.update {
                    it.copy(domain = result, isUpdating = false, isUpdated = true)
                }
            }, onFailure = { error ->
                _stateFlow.update {
                    it.copy(isUpdating = false, updateError = error)
                }
            })
        }
    }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.(ApiKey) -> Unit
    ) = scope.launch { block(ApiKey(apiKeyValue)) }
}