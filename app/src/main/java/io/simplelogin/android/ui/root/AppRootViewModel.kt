package io.simplelogin.android.ui.root

import android.content.Context
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation3.runtime.NavKey
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.data.models.preferences.LockTimeOut
import io.simplelogin.android.di.AppVersion
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AppRootViewModel @Inject constructor(
    @AppVersion val appVersion: String,
    @ApplicationContext private val context: Context,
    observeSessionSettingsUseCase: ObserveSessionSettingsUseCase,
    private val updateSessionSettingsUseCase: UpdateSessionSettingsUseCase,
    private val snackbarManager: SnackbarManager,
    @LoadingState private val loadingState: LoadingStateFlow,
): ViewModel() {

    private val _navBackStack = MutableStateFlow(mutableStateListOf<NavKey>(InitializationDestination))
    val navBackStack = _navBackStack.asStateFlow()

    var showDeviceSettingsDialog = MutableStateFlow(false)
    var showLogOutDialog = MutableStateFlow(false)

    val stateFlow: StateFlow<AppRootState> = observeSessionSettingsUseCase()
        .map {
            AppRootState(
                isReady = true,
                apiKey = it.apiKey
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppRootState.Default
        )

    //region Setup
    init {
        viewModelScope.launch {
            stateFlow
                .collect {
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
    //endregion

    //region Log in/sign up
    fun updateApiKey(apiKey: String?) {
        viewModelScope.launch {
            updateSessionSettingsUseCase.invoke {
                if (apiKey == null) {
                    // Log out, remove API key and reset lock settings
                    it.copy(
                        apiKey = null,
                        lockEnabled = false,
                        lockTimeOut = LockTimeOut.DEFAULT
                    )
                } else {
                    // Login
                    it.copy(apiKey = apiKey)
                }
            }
        }
    }
    //endregion

    //region Drawer
    fun showLogOutDialog() {
        showLogOutDialog.value = true
    }

    fun dismissLogOutDialog() {
        showLogOutDialog.value = false
    }

    fun logOut() {
        showLogOutDialog.value = false
        updateApiKey(null)
    }

    fun showDeviceSettingsDialog() {
        showDeviceSettingsDialog.value = true
    }
    //endregion

    //region Home
    fun viewAliasDetail(aliasId: String) {
        _navBackStack.value.apply {
            // Workaround crashes by clearing the backStack
            clear()
            add(HomeDestination("Some API Key"))
            add(AliasDetail(aliasId))
        }
    }
    //endregion
}