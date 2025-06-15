package io.simplelogin.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.remote.BaseUrlProvider
import io.simplelogin.android.ui.AppRoot
import io.simplelogin.android.ui.AppRootViewModel
import io.simplelogin.android.ui.theme.SimpleLoginTheme
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val appRootViewModel: AppRootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel.observe()
        setUpSplashScreen()
        applyOrientationRestrictions()
        enableEdgeToEdge()
        setContent {
            SimpleLoginTheme {
                val snackbarHostState = remember { SnackbarHostState() }
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    snackbarHost = { SnackbarHost(snackbarHostState) }
                ) { innerPadding ->
                    AppRoot(
                        modifier = Modifier.padding(innerPadding),
                        snackbarHostState = snackbarHostState,
                        viewModel = appRootViewModel
                    )
                }
            }
        }
    }

    private fun setUpSplashScreen() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !appRootViewModel.isAppReady.value }
    }

    private fun applyOrientationRestrictions() {
        val configuration = resources.configuration
        val isTablet = configuration.smallestScreenWidthDp >= 600
        requestedOrientation = if (isTablet) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }
    }
}

// TODO: Convert to a use case
@HiltViewModel
class MainViewModel @Inject constructor(
    private val baseUrlProvider: BaseUrlProvider,
    private val userSessionPreferences: DataStore<UserSessionPreferences>
): ViewModel() {
    fun observe() {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    baseUrlProvider.updateBaseUrl(it.baseUrl)
                }
        }
    }
}