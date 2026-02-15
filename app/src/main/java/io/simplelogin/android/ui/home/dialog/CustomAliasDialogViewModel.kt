package io.simplelogin.android.ui.home.dialog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.data.remote.datasource.CreationRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class CustomAliasDialogViewModel @Inject constructor(
    @LoadingState private val loadingState: LoadingStateFlow,
    private val datasource: CreationRemoteDatasource,
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
                loadingState.emit(true)
                when (val options = datasource.getAliasOptions(apiKey = apiKey)) {
                    is Result.Success -> _stateFlow.update { state ->
                        val sortedSuffixes = options.value.suffixes.sortedWith(
                            compareByDescending<Suffix> { it.isCustom }
                                .thenByDescending { it.isPremium }
                        )
                        state.copy(aliasOptions = options.value.copy(suffixes = sortedSuffixes))
                    }

                    is Result.Failure -> _stateFlow.update { state ->
                        state.copy(fetchError = options.error)
                    }
                }
                loadingState.emit(false)
            }
        }
    }
}