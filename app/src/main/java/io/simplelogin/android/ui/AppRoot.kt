package io.simplelogin.android.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import androidx.hilt.navigation.compose.hiltViewModel
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
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.login.LaunchScreen
import io.simplelogin.android.ui.login.LoginScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import javax.inject.Inject

sealed class AppRootDestination: NavKey {
    @Serializable
    object Launching: AppRootDestination()
    @Serializable
    object LogIn: AppRootDestination()
    @Serializable
    data class Home(val apiKey: String): AppRootDestination()
}

@Composable
fun AppRoot(modifier: Modifier = Modifier,
            viewModel: AppRootViewModel = hiltViewModel()
) {
    val backStack = rememberNavBackStack(AppRootDestination.Launching)
    val baseUrl by viewModel.baseUrl.collectAsState()

    LaunchedEffect(backStack) {
        viewModel.setNavBackStack(backStack)
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
                    AppRootDestination.Launching -> {
                        NavEntry(navKey) {
                            LaunchScreen(modifier)
                        }
                    }

                    AppRootDestination.LogIn -> {
                        NavEntry(navKey) {
                            LoginScreen(
                                modifier = modifier,
                                baseUrl = baseUrl,
                                onLoginClick = viewModel::logIn,
                                onBaseUrlChange = viewModel::updateBaseUrl
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
    private var _navBackStack: NavBackStack? = null
    private var _baseUrl = MutableStateFlow<String>(Constants.DEFAULT_BASE_URL)
    val baseUrl = _baseUrl.asStateFlow()

    init {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    _baseUrl.emit(it.baseUrl)
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

    private fun updateApiKey(apiKey: String?) {
        viewModelScope.launch {
            userSessionPreferences.updateData {
                UserSessionPreferences(baseUrl = it.baseUrl, apiKey = apiKey)
            }
        }
    }
}