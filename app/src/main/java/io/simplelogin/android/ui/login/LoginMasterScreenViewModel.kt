package io.simplelogin.android.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginMasterScreenViewModel @Inject constructor(
    @AppVersion val appVersion: String,
    @LoadingState private val loadingState: LoadingStateFlow,
    observeSessionSettings: ObserveSessionSettingsUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase
) : ViewModel() {
    val baseUrlState: StateFlow<String> = observeSessionSettings()
        .map { it.baseUrl }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = Constants.DEFAULT_BASE_URL
        )

    fun login(email: String, password: String) {
        viewModelScope.launch {
            loadingState.emit(true)
            updateSessionSettings.invoke {
                it.copy(apiKey = "Some key")
            }
            loadingState.emit(false)
        }
    }

    fun login(apiKey: String) {
        print(apiKey)
    }

    fun signUp(email: String, password: String) {
        print("$email - $password")
    }

    fun forgotPassword(email: String) {
        print(email)
    }

    fun resentActivationCode(email: String) {
        print(email)
    }

    fun updateBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            updateSessionSettings.invoke {
                it.copy(baseUrl = baseUrl)
            }
        }
    }
}