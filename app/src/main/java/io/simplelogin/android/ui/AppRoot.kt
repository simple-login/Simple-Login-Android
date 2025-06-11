package io.simplelogin.android.ui

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import io.simplelogin.android.ui.login.LaunchScreen
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
                            Text("Login")
                        }
                    }

                    is AppRootDestination.Home -> {
                        NavEntry(navKey) {
                            Text("Home ${navKey.apiKey}")
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
    userSessionPreferences: DataStore<UserSessionPreferences>
): ViewModel() {
    private var _navBackStack: NavBackStack? = null

    init {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    assert(_navBackStack != null) { "NavBackStack is not set" }
                    _navBackStack?.apply {
                        clear()
                        val apiKey = it.apiKey
                        if (apiKey != null) {
                            _navBackStack?.add(AppRootDestination.Home(apiKey))
                        } else {
                            _navBackStack?.add(AppRootDestination.LogIn)
                        }
                    }
                }
        }
    }

    fun setNavBackStack(navBackStack: NavBackStack) {
        _navBackStack = navBackStack
    }
}