package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.CustomDomain
import io.simplelogin.core.network.datasource.CustomDomainsRemoteDatasource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomDomainDeletedAliasesViewModel.Factory::class)
class CustomDomainDeletedAliasesViewModel @AssistedInject constructor(
    @Assisted private val domain: CustomDomain,
    @Assisted private val apiKeyValue: String,
    private val datasource: CustomDomainsRemoteDatasource
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(domain: CustomDomain, apiKeyValue: String): CustomDomainDeletedAliasesViewModel
    }

    private val _stateFlow = MutableStateFlow(CustomDomainDeletedAliasesState.Default)
    val stateFlow: StateFlow<CustomDomainDeletedAliasesState> = _stateFlow

    init {
        fetchDeletedAliases()
    }

    fun fetchDeletedAliases() {
        _stateFlow.update { it.copy(isFetching = true, fetchError = null) }
        withApiKey { apiKey ->
            datasource.getDeletedAliases(apiKey = apiKey, domain = domain)
                .fold(onSuccess = { result ->
                    _stateFlow.update {
                        it.copy(aliases = result, isFetching = false, fetchError = null)
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(aliases = null, isFetching = false, fetchError = error)
                    }
                })
        }
    }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.(ApiKey) -> Unit
    ) = scope.launch { block(ApiKey(apiKeyValue)) }
}