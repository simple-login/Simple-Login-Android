package io.simplelogin.android.ui.login

import android.content.Context
import android.webkit.CookieManager
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.remote.BaseUrlProvider
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.designsystem.description
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.models.Constants
import io.simplelogin.android.models.api.ApiKey
import io.simplelogin.android.usecases.ShowSnackbarFailureUseCase
import io.simplelogin.android.usecases.ShowSnackbarInformationUseCase
import io.simplelogin.android.usecases.login.ForgotPasswordUseCase
import io.simplelogin.android.usecases.login.LogInError
import io.simplelogin.android.usecases.login.LogInUseCase
import io.simplelogin.android.usecases.login.ResendActivationCodeUseCase
import io.simplelogin.android.usecases.login.SignUpUseCase
import io.simplelogin.android.usecases.login.VerifyAccountUseCase
import io.simplelogin.android.usecases.login.VerifyMfaUseCase
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import io.simplelogin.android.util.ProtonLoginManager
import io.simplelogin.android.util.isValidEmail
import io.simplelogin.android.util.isValidPassword
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AccountActivationPayload(
    val email: String,
    val password: String
)

@HiltViewModel
class LoginMasterScreenViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    @LoadingState private val loadingState: LoadingStateFlow,
    private val logIn: LogInUseCase,
    private val verifyMfa: VerifyMfaUseCase,
    private val verifyAccount: VerifyAccountUseCase,
    private val forgotPassword: ForgotPasswordUseCase,
    private val signUp: SignUpUseCase,
    private val showSnackbarInformation: ShowSnackbarInformationUseCase,
    private val showSnackbarFailure: ShowSnackbarFailureUseCase,
    private val resendActivationCode: ResendActivationCodeUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase,
    private val accountSettingsRemoteDatasource: AccountSettingsRemoteDatasource,
    private val baseUrlProvider: BaseUrlProvider,
    private val protonLoginManager: ProtonLoginManager,
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

    init {
        viewModelScope.launch {
            protonLoginManager.pendingApiKey.collect { apiKey ->
                login(ApiKey(value = apiKey))
            }
        }
    }

    fun launchLoginWithProton() {
        CookieManager.getInstance().removeAllCookies(null)
        val baseUrl = baseUrlProvider.getBaseUrl()
        val scheme = context.getString(R.string.simplelogin_scheme)
        val url = "$baseUrl/auth/proton/login?mode=apikey&action=login&scheme=$scheme&next=/login"
        val customTabsIntent = CustomTabsIntent.Builder().build()
        customTabsIntent.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, url.toUri())
    }

    private val _mfaKeyStateFlow = MutableStateFlow<String?>(null)
    val mfaKeyStateFlow: StateFlow<String?> = _mfaKeyStateFlow

    private val _accountActivationPayloadStateFlow =
        MutableStateFlow<AccountActivationPayload?>(null)
    val accountActivationPayloadStateFlow: StateFlow<AccountActivationPayload?> =
        _accountActivationPayloadStateFlow

    fun login(email: String, password: String) {
        val errorMessage = when {
            !email.isValidEmail() -> context.getString(R.string.enter_valid_email)
            !password.isValidPassword() -> context.getString(R.string.enter_valid_password)
            else -> null
        }

        if (errorMessage != null) {
            viewModelScope.launch {
                showSnackbarFailure(errorMessage)
            }
            return
        }

        launchLoading(doWork = {
            logIn(email = email, password = password)
        }, handleResult = { result ->
            when (result) {
                is Result.Success -> {
                    val userLogin = result.value
                    userLogin.apiKey?.let { apiKeyValue ->
                        login(apiKey = ApiKey(value = apiKeyValue))
                    } ?: userLogin.mfaKey?.let { mfaKey ->
                        _mfaKeyStateFlow.value = mfaKey
                    }
                }

                is Result.Failure -> {
                    when (result.error) {
                        is LogInError.IncorrectEmailOrPassword -> {
                            val message = context.getString(R.string.incorrect_email_or_password)
                            showSnackbarFailure(message)
                        }

                        is LogInError.AccountNotActivated ->
                            _accountActivationPayloadStateFlow.value = AccountActivationPayload(
                                email = email,
                                password = password
                            )

                        is LogInError.Api -> {
                            val message = result.error.error.description(context)
                            showSnackbarFailure(message)
                        }
                    }
                }
            }
        })
    }

    fun dismissAccountActivation() {
        _accountActivationPayloadStateFlow.value = null
    }

    fun activateAccount(email: String, password: String, code: String) {
        launchLoading(doWork = {
            verifyAccount(email = email, code = code)
        }, handleResult = { result ->
            when (result) {
                is Result.Success -> login(email = email, password = password)
                is Result.Failure -> showSnackbarFailure(result.error.description(context))
            }
        })
    }

    fun confirmMfa(token: String, key: String) {
        launchLoading(doWork = {
            verifyMfa(token = token, key = key)
        }, handleResult = {
            when (it) {
                is Result.Success -> login(it.value)
                is Result.Failure -> showSnackbarFailure(it.error.description(context))
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
                showSnackbarFailure(error.description(context))
            })
        })
    }

    fun signUp(email: String, password: String) {
        launchLoading(doWork = {
            signUp.invoke(email = email, password = password)
        }, handleResult = {
            when (it) {
                is Result.Success -> {
                    val message = context.getString(R.string.confirm_email_instructions_sent, email)
                    showSnackbarInformation(message)
                }

                is Result.Failure -> showSnackbarFailure(it.error.description(context))
            }
        })
    }

    fun forgotPassword(email: String) {
        launchLoading(doWork = {
            forgotPassword.invoke(email)
        }, handleResult = {
            when (it) {
                is Result.Success -> {
                    val message =
                        context.getString(R.string.password_reset_instructions_sent, email)
                    showSnackbarInformation(message)
                }

                is Result.Failure -> showSnackbarFailure(it.error.description(context))
            }
        })
    }

    fun resentActivationCode(email: String) {
        launchLoading(doWork = {
            resendActivationCode(email)
        }, handleResult = {
            when (it) {
                is Result.Success -> {
                    val message = context.getString(R.string.resend_activation_code_done, email)
                    showSnackbarInformation(message)
                }

                is Result.Failure -> showSnackbarFailure(it.error.description(context))
            }
        })
    }

    fun updateBaseUrl(baseUrl: String) {
        viewModelScope.launch {
            updateSessionSettings.invoke {
                it.copy(baseUrl = baseUrl)
            }
            showSnackbarInformation(context.getString(R.string.api_url_updated))
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
}