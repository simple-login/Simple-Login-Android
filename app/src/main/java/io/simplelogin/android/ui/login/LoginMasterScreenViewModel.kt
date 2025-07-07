package io.simplelogin.android.ui.login

import android.content.Context
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.usecases.login.LogInError
import io.simplelogin.android.usecases.login.LogInUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginMasterScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val snackbarManager: SnackbarManager,
    private val logIn: LogInUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase,
    @AppVersion val appVersion: String,
    observeSessionSettings: ObserveSessionSettingsUseCase
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
            val deviceName = "${Build.MANUFACTURER} ${Build.MODEL} ${Build.DEVICE}"
            val result = logIn.invoke(email = email, password = password, deviceName = deviceName)
            loadingState.emit(false)
            when (result) {
                is Result.Success -> {
                    result.value
                }

                is Result.Failure -> {
                    val message = when (result.error) {
                        is LogInError.IncorrectEmailOrPassword -> context.getString(R.string.incorrect_email_or_password)
                        is LogInError.Api -> result.error.error.description(context)
                    }
                    snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                }
            }
        }
    }

    fun login(apiKey: String) {
        viewModelScope.launch {
            loadingState.emit(true)
            delay(2_000)
            loadingState.emit(false)
        }
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
            snackbarManager.showSnackbar(SnackbarConfiguration(
                message = context.getString(R.string.api_url_updated)
            ))
        }
    }
}