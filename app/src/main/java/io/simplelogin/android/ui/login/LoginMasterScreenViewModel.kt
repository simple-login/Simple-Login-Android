package io.simplelogin.android.ui.login

import android.content.Context
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
import io.simplelogin.android.usecases.login.ForgotPasswordUseCase
import io.simplelogin.android.usecases.login.LogInError
import io.simplelogin.android.usecases.login.LogInUseCase
import io.simplelogin.android.usecases.login.SignUpUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import io.simplelogin.android.util.isValidEmail
import io.simplelogin.android.util.isValidPassword
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
    private val logInUseCase: LogInUseCase,
    private val forgotPasswordUseCase: ForgotPasswordUseCase,
    private val signUpUseCase: SignUpUseCase,
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
        if (!email.isValidEmail()) {
            val message = context.getString(R.string.enter_valid_email)
            showSnackbar(message)
            return
        }

        if (!password.isValidPassword()) {
            val message = context.getString(R.string.enter_valid_password)
            showSnackbar(message)
            return
        }

        launchLoading(doWork = {
            logInUseCase.invoke(email = email, password = password)
        }, handleResult = {
            when (it) {
                is Result.Success -> {
                    it.value
                }

                is Result.Failure -> {
                    val message = when (it.error) {
                        is LogInError.IncorrectEmailOrPassword -> context.getString(R.string.incorrect_email_or_password)
                        is LogInError.Api -> it.error.error.description(context)
                    }
                    snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
                }
            }
        })
    }

    fun login(apiKey: String) {
        viewModelScope.launch {
            loadingState.emit(true)
            delay(2_000)
            loadingState.emit(false)
        }
    }

    fun signUp(email: String, password: String) {
        launchLoading(doWork = {
            signUpUseCase.invoke(email = email, password = password)
        }, handleResult = {
            val message = when (it) {
                is Result.Success -> context.getString(R.string.confirm_email_instructions_sent, email)
                is Result.Failure -> it.error.description(context)
            }
            showSnackbar(message)
        })
    }

    fun forgotPassword(email: String) {
        launchLoading(doWork = {
            forgotPasswordUseCase.invoke(email)
        }, handleResult = {
            val message = when (it) {
                is Result.Success -> context.getString(R.string.password_reset_instructions_sent, email)
                is Result.Failure -> it.error.description(context)
            }
            showSnackbar(message)
        })
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

    private fun <T> launchLoading(doWork: suspend () -> T, handleResult: suspend (T) -> Unit) {
        viewModelScope.launch {
            loadingState.emit(true)
            val result = doWork()
            loadingState.emit(false)
            handleResult(result)
        }
    }

    private fun showSnackbar(message: String) {
        viewModelScope.launch {
            snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
        }
    }
}