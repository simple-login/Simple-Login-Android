package io.simplelogin.android.ui.login

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.SnackbarType
import io.simplelogin.android.usecases.login.ForgotPasswordUseCase
import io.simplelogin.android.usecases.login.LogInError
import io.simplelogin.android.usecases.login.LogInUseCase
import io.simplelogin.android.usecases.login.ResendActivationCodeUseCase
import io.simplelogin.android.usecases.login.SignUpUseCase
import io.simplelogin.android.usecases.login.VerifyMfaUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import io.simplelogin.android.util.isValidEmail
import io.simplelogin.android.util.isValidPassword
import kotlinx.coroutines.flow.MutableStateFlow
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
    private val verifyMfa: VerifyMfaUseCase,
    private val forgotPassword: ForgotPasswordUseCase,
    private val signUp: SignUpUseCase,
    private val resendActivationCode: ResendActivationCodeUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase,
    private val accountSettingsRemoteDatasource: AccountSettingsRemoteDatasource,
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

    private val _mfaKeyStateFlow = MutableStateFlow<String?>(null)
    val mfaKeyStateFlow: StateFlow<String?> = _mfaKeyStateFlow

    fun login(email: String, password: String) {
        val errorMessage = when {
            !email.isValidEmail() -> context.getString(R.string.enter_valid_email)
            !password.isValidPassword() -> context.getString(R.string.enter_valid_password)
            else -> null
        }

        if (errorMessage != null) {
            viewModelScope.launch {
                showSnackbar(errorMessage)
            }
            return
        }

        launchLoading(doWork = {
            logIn(email = email, password = password)
        }, handleResult = {
            when (it) {
                is Result.Success -> {
                    val userLogin = it.value
                    userLogin.apiKey?.let { apiKeyValue ->
                        login(apiKey = ApiKey(value = apiKeyValue))
                    } ?: userLogin.mfaKey?.let { mfaKey ->
                        _mfaKeyStateFlow.value = mfaKey
                    }
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

    fun confirmMfa(token: String, key: String) {
        launchLoading(doWork = {
            verifyMfa(token = token, key = key)
        }, handleResult = {
            when (it) {
                is Result.Success -> login(it.value)
                is Result.Failure ->
                    snackbarManager.showSnackbar(
                        SnackbarConfiguration(
                            message = it.error.description(context),
                            type = SnackbarType.FAILURE
                        )
                    )
            }
        })
    }

    fun dismissMfaVerification() {
        _mfaKeyStateFlow.value = null
    }

    fun login(apiKey: ApiKey) {
        launchLoading(doWork = {
            accountSettingsRemoteDatasource.getUserInfo(apiKey)
        }, handleResult = { result ->
            result.fold(onSuccess = { userInfo ->
                updateSessionSettings { it.copy(apiKey = apiKey, userInfo = userInfo) }
            }, onFailure = { error ->
                showSnackbar(error.description(context))
            })
        })
    }

    fun signUp(email: String, password: String) {
        launchLoading(doWork = {
            signUp.invoke(email = email, password = password)
        }, handleResult = {
            val message = when (it) {
                is Result.Success ->
                    context.getString(R.string.confirm_email_instructions_sent, email)

                is Result.Failure -> it.error.description(context)
            }
            showSnackbar(message)
        })
    }

    fun forgotPassword(email: String) {
        launchLoading(doWork = {
            forgotPassword.invoke(email)
        }, handleResult = {
            val message = when (it) {
                is Result.Success ->
                    context.getString(R.string.password_reset_instructions_sent, email)

                is Result.Failure -> it.error.description(context)
            }
            showSnackbar(message)
        })
    }

    fun resentActivationCode(email: String) {
        launchLoading(doWork = {
            resendActivationCode(email)
        }, handleResult = {
            val message = when (it) {
                is Result.Success ->
                    context.getString(R.string.resend_activation_code_done, email)

                is Result.Failure -> it.error.description(context)
            }
            showSnackbar(message)
        })
    }

    fun updateBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            updateSessionSettings.invoke {
                it.copy(baseUrl = baseUrl)
            }
            showSnackbar(context.getString(R.string.api_url_updated))
        }
    }

    private fun <T> launchLoading(doWork: suspend () -> T, handleResult: suspend (T) -> Unit) {
        viewModelScope.launch {
            loadingState.value = true
            val result = doWork()
            loadingState.value = false
            handleResult(result)
        }
    }

    private suspend fun showSnackbar(message: String) {
        snackbarManager.showSnackbar(SnackbarConfiguration(message = message))
    }
}