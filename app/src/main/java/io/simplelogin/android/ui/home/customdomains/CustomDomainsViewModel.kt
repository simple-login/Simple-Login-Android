package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.android.models.api.ApiKey
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = CustomDomainsViewModel.Factory::class)
class CustomDomainsViewModel @AssistedInject constructor(
    @Assisted private val apiKeyValue: String,
    private val datasource: CustomDomainsRemoteDatasource
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(apiKeyValue: String): CustomDomainsViewModel
    }

    private val _stateFlow = MutableStateFlow(CustomDomainsState.Default)
    val stateFlow: StateFlow<CustomDomainsState> = _stateFlow

    init {
        fetchCustomDomains()
    }

    fun fetchCustomDomains() {
        _stateFlow.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            datasource.getCustomDomains(ApiKey(apiKeyValue))
                .fold(onSuccess = { result ->
                    _stateFlow.update {
                        it.copy(domains = result.value, isFetching = false)
                    }
                }, onFailure = { error ->
                    _stateFlow.update {
                        it.copy(isFetching = false, fetchError = error)
                    }
                })
        }
    }
}