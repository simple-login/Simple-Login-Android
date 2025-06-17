package io.simplelogin.android.ui

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarConfiguration
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.login.LoginScreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

@Serializable
data object InitializationDestination: NavKey

@Serializable
data object LogInDestination: NavKey

@Serializable
data class HomeDestination(val apiKey: String): NavKey

@Composable
fun AppRoot(modifier: Modifier = Modifier,
            viewModel: AppRootViewModel
) {
    val baseUrl by viewModel.baseUrl.collectAsState()
    val backStack by viewModel.navBackStack.collectAsState()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ),
        entryProvider = entryProvider {
            entry<InitializationDestination> {}

            entry<LogInDestination> {
                LoginScreen(
                    modifier = modifier,
                    appVersion = viewModel.appVersion,
                    baseUrl = baseUrl,
                    onBaseUrlChange = viewModel::updateBaseUrl,
                    onLoginClick = viewModel::logIn,
                    onLoginWithProtonClick = viewModel::logInWithProton,
                    onLoginWithApiKeyClick = viewModel::updateApiKey,
                    onForgotPassword = viewModel::resetPassword,
                    onSignUp = viewModel::createAccount,
                    onResendActivationCode = viewModel::resendActivationCode
                )
            }

            entry<HomeDestination> { key ->
                HomeScreen(
                    modifier = modifier,
                    apiKey = key.apiKey,
                    onLogOutClick = viewModel::logOut,
                )
            }
        }
    )
}

@HiltViewModel
class AppRootViewModel @Inject constructor(
    @AppVersion val appVersion: String,
    @ApplicationContext private val context: Context,
    private val userSessionPreferences: DataStore<UserSessionPreferences>,
    private val snackbarManager: SnackbarManager,
    @LoadingState private val loadingState: LoadingStateFlow,
): ViewModel() {
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady = _isAppReady.asStateFlow()

    private val _baseUrl = MutableStateFlow<String>(Constants.DEFAULT_BASE_URL)
    val baseUrl = _baseUrl.asStateFlow()

    private val _navBackStack = MutableStateFlow(mutableStateListOf<NavKey>(InitializationDestination))
    val navBackStack = _navBackStack.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    _isAppReady.value = true
                    _baseUrl.value = it.baseUrl
                    _navBackStack.value.apply {
                        clear()
                        val apiKey = it.apiKey
                        if (apiKey != null) {
                            add(HomeDestination(apiKey))
                        } else {
                            add(LogInDestination)
                        }
                    }
                }
        }
    }

    fun setSnackbarHostState(snackbarHostState: SnackbarHostState) {
        viewModelScope.launch {
            snackbarManager.configuration
                .collect { configuration ->
                    val result = snackbarHostState.showSnackbar(
                        message = configuration.message,
                        actionLabel = configuration.action?.label,
                        withDismissAction = configuration.duration == SnackbarDuration.Indefinite,
                        duration = configuration.duration
                    )

                    when (result) {
                        SnackbarResult.ActionPerformed -> {
                            configuration.action?.action?.let { it() }
                        }

                        else -> Unit
                    }
                }
        }
    }

    fun logIn() {
        updateApiKey("Some API key")
    }

    fun logInWithProton() {
        viewModelScope.launch {
            loadingState.emit(true)
            delay(2000)
            loadingState.emit(false)
        }
    }

    fun logOut() {
        updateApiKey(null)
    }

    fun createAccount(email: String, password: String) = Unit

    fun updateBaseUrl(newBaseUrl: String) {
        viewModelScope.launch {
            userSessionPreferences.updateData {
                UserSessionPreferences(baseUrl = newBaseUrl, apiKey = it.apiKey)
            }
        }
    }

    fun updateApiKey(apiKey: String?) {
        viewModelScope.launch {
            userSessionPreferences.updateData {
                UserSessionPreferences(baseUrl = it.baseUrl, apiKey = apiKey)
            }
        }
    }

    fun resetPassword(emailAddress: String) {
        viewModelScope.launch {
            val message = context.getString(R.string.reset_password_confirmation, emailAddress)
            snackbarManager.showSnackbar(SnackbarConfiguration(message))
        }
    }

    fun resendActivationCode(emailAddress: String) = Unit
}