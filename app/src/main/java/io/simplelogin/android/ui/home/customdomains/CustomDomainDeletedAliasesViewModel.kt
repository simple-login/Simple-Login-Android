package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomDomainDeletedAliasesViewModel.Factory::class)
class CustomDomainDeletedAliasesViewModel @AssistedInject constructor(
    @Assisted private val domain: CustomDomain,
    private val datasource: CustomDomainsRemoteDatasource,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(domain: CustomDomain): CustomDomainDeletedAliasesViewModel
    }

    private val _stateFlow = MutableStateFlow(CustomDomainDeletedAliasesState.Default)
    val stateFlow: StateFlow<CustomDomainDeletedAliasesState> = _stateFlow

    init {
        fetchDeletedAliases()
    }

    fun fetchDeletedAliases() {
        _stateFlow.update { it.copy(isFetching = true, fetchError = null) }
        withApiKey { apiKey ->
            val result = datasource.getDeletedAliases(apiKey = apiKey, domain = domain)
            when (result) {
                is Result.Success -> _stateFlow.update {
                    it.copy(aliases = result.value, isFetching = false, fetchError = null)
                }

                is Result.Failure -> _stateFlow.update {
                    it.copy(aliases = null, isFetching = false, fetchError = result.error)
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