package io.simplelogin.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val appRootViewModel: AppRootViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !appRootViewModel.isAppReady.value }
        enableEdgeToEdge()
        setContent {
            SimpleLoginTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppRoot(
                        modifier = Modifier.padding(innerPadding),
                        viewModel = appRootViewModel
                    )
                }
            }
        }
    }
}

// TODO: Convert to a use case
@HiltViewModel
class MainViewModel @Inject constructor(
    baseUrlProvider: BaseUrlProvider,
    userSessionPreferences: DataStore<UserSessionPreferences>
): ViewModel() {
    init {
        viewModelScope.launch {
            userSessionPreferences.data
                // Only listen to baseUrl changes
                .distinctUntilChanged { old, new -> old.baseUrl == new.baseUrl }
                .collect {
                    baseUrlProvider.updateBaseUrl(it.baseUrl)
                }
        }
    }
}