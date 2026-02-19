package io.simplelogin.android.ui.home.customdomains

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.remote.datasource.CustomDomainsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CustomDomainsViewModel @Inject constructor(
    private val datasource: CustomDomainsRemoteDatasource,
    private val observeSessionSettings: ObserveSessionSettingsUseCase
) : ViewModel() {
    private val _stateFlow = MutableStateFlow(CustomDomainsState.Default)
    val stateFlow: StateFlow<CustomDomainsState> = _stateFlow

    init {
        fetchCustomDomains()
    }

    fun fetchCustomDomains() {
        _stateFlow.update { it.copy(isFetching = true) }
        viewModelScope.launch {
            observeSessionSettings().collect { settings ->
                settings.apiKey?.let { apiKey ->
                    when (val result = datasource.getCustomDomains(apiKey)) {
                        is Result.Success -> _stateFlow.update {
                            it.copy(
                                domains = result.value.value,
                                isFetching = false
                            )
                        }

                        is Result.Failure -> _stateFlow.update {
                            it.copy(
                                isFetching = false,
                                fetchError = result.error
                            )
                        }
                    }
                }
            }
        }
    }
}