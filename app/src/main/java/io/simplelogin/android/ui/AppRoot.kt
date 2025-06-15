package io.simplelogin.android.ui

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.BuildConfig
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.login.LoginScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

sealed class AppRootDestination: NavKey {
    @Serializable
    object Initialization: AppRootDestination()
    @Serializable
    object LogIn: AppRootDestination()
    @Serializable
    data class Home(val apiKey: String): AppRootDestination()
}

@Composable
fun AppRoot(modifier: Modifier = Modifier,
            snackbarHostState: SnackbarHostState,
            viewModel: AppRootViewModel
) {
    val context = LocalContext.current
    val backStack = rememberNavBackStack(AppRootDestination.Initialization)
    val baseUrl by viewModel.baseUrl.collectAsState()

    LaunchedEffect(backStack) {
        viewModel.setNavBackStack(backStack)
    }

    LaunchedEffect(Unit) {
        viewModel.resetPasswordEmail.collectLatest { email ->
            if (email != null) {
                snackbarHostState.showSnackbar(
                    message = context.getString(R.string.reset_password_confirmation, email)
                )
            }
        }
    }

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        entryDecorators = listOf(
            rememberSavedStateNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
            rememberSceneSetupNavEntryDecorator()
        ),
        entryProvider = { navKey ->
            if (navKey is AppRootDestination) {
                return@NavDisplay when(navKey) {
                    AppRootDestination.Initialization -> {
                        NavEntry(navKey) {
                            // Dummy empty screen while deciding if the user is logged in or not
                            // Rely on splash screen and show nothing here
                        }
                    }

                    AppRootDestination.LogIn -> {
                        NavEntry(navKey) {
                            LoginScreen(
                                modifier = modifier,
                                appVersion = viewModel.appVersion,
                                baseUrl = baseUrl,
                                onBaseUrlChange = viewModel::updateBaseUrl,
                                onLoginClick = viewModel::logIn,
                                onLoginWithProtonClick = viewModel::logInWithProton,
                                onLoginWithApiKeyClick = viewModel::updateApiKey,
                                onForgotPassword = viewModel::resetPassword
                            )
                        }
                    }

                    is AppRootDestination.Home -> {
                        NavEntry(navKey) {
                            HomeScreen(
                                modifier = modifier,
                                apiKey = navKey.apiKey,
                                onLogOutClick = viewModel::logOut,
                            )
                        }
                    }
                }
            } else {
                throw RuntimeException("NavKey must be of type AppRootDestination")
            }
        }
    )
}

@HiltViewModel
class AppRootViewModel @Inject constructor(
    private val userSessionPreferences: DataStore<UserSessionPreferences>
): ViewModel() {
    private val _isAppReady = MutableStateFlow(false)
    val isAppReady = _isAppReady.asStateFlow()

    private var _navBackStack: NavBackStack? = null
    private val _baseUrl = MutableStateFlow<String>(Constants.DEFAULT_BASE_URL)
    val baseUrl = _baseUrl.asStateFlow()

    private var _resetPasswordEmail = MutableStateFlow<String?>(null)
    val resetPasswordEmail = _resetPasswordEmail.asStateFlow()

    val appVersion = "v${BuildConfig.VERSION_NAME}-${BuildConfig.FLAVOR}"

    init {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    _isAppReady.value = true
                    _baseUrl.value = it.baseUrl
                    assert(_navBackStack != null) { "NavBackStack is not set" }
                    _navBackStack?.apply {
                        clear()
                        val apiKey = it.apiKey
                        if (apiKey != null) {
                            add(AppRootDestination.Home(apiKey))
                        } else {
                            add(AppRootDestination.LogIn)
                        }
                    }
                }
        }
    }

    fun setNavBackStack(navBackStack: NavBackStack) {
        _navBackStack = navBackStack
    }

    fun logIn() {
        updateApiKey("Some API key")
    }

    fun logInWithProton() = Unit

    fun logOut() {
        updateApiKey(null)
    }

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
            _resetPasswordEmail.emit(emailAddress)
        }
    }
}