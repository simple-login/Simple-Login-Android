package io.simplelogin.android

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDefaults
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.datastore.core.DataStore
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.data.remote.BaseUrlProvider
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.colors
import io.simplelogin.android.ui.root.AppRoot
import io.simplelogin.android.ui.root.AppRootViewModel
import io.simplelogin.android.ui.root.supportsMultiplePanes
import io.simplelogin.android.ui.theme.SimpleLoginTheme
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.asStateFlow
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
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()

            fun closeDrawerAndExecute(task: () -> Unit) {
                scope.launch {
                    drawerState.close()
                    task()
                }
            }

            SimpleLoginTheme {
                ModalNavigationDrawer(
                    drawerState = drawerState,
                    drawerContent = {
                        Drawer(
                            appVersion = appRootViewModel.appVersion,
                            onDeviceSettingsClick = {
                                val asDialog = windowAdaptiveInfo.supportsMultiplePanes()
                                closeDrawerAndExecute {
                                    appRootViewModel.showDeviceSettingsScreen(asDialog = asDialog)
                                }
                            },
                            onSignOutClick = {
                                closeDrawerAndExecute(appRootViewModel::showLogOutDialog)
                            }
                        )
                    },
                    content = {
                        MainUi(
                            drawerState = drawerState,
                            viewModel = viewModel,
                            appRootViewModel = appRootViewModel
                        )
                    }
                )
            }
        }
    }

    private fun setUpSplashScreen() {
        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition { !appRootViewModel.stateFlow.value.isReady }
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
    @LoadingState private val loadingState: LoadingStateFlow,
    private val baseUrlProvider: BaseUrlProvider,
    private val userSessionPreferences: DataStore<UserSessionPreferences>,
    val snackbarManager: SnackbarManager
) : ViewModel() {
    val showLoadingIndicator = loadingState.asStateFlow()

    fun observe() {
        viewModelScope.launch {
            userSessionPreferences.data
                .collect {
                    baseUrlProvider.updateBaseUrl(it.baseUrl)
                }
        }
    }
}

@Composable
private fun MainUi(
    drawerState: DrawerState,
    viewModel: MainViewModel,
    appRootViewModel: AppRootViewModel
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val isLoading by viewModel.showLoadingIndicator.collectAsState()
    var currentSnackbarJob by remember { mutableStateOf<Job?>(null) }

    LaunchedEffect(Unit) {
        viewModel.snackbarManager.configuration.collect { configuration ->
            currentSnackbarJob?.cancel()
            currentSnackbarJob = scope.launch {
                val result = snackbarHostState.showSnackbar(visuals = configuration.toVisuals())

                when (result) {
                    SnackbarResult.ActionPerformed -> {
                        configuration.action?.action?.let { it() }
                    }

                    else -> Unit
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                snackbar = { data ->
                    val colors = data.visuals.colors()
                    Snackbar(
                        snackbarData = data,
                        containerColor = colors.containerColor ?: SnackbarDefaults.color,
                        contentColor = colors.contentColor ?: SnackbarDefaults.contentColor,
                        actionColor = colors.actionColor ?: SnackbarDefaults.actionColor,
                        actionContentColor = colors.actionContentColor
                            ?: SnackbarDefaults.actionContentColor,
                        dismissActionContentColor = colors.dismissActionContentColor
                            ?: SnackbarDefaults.dismissActionContentColor,
                    )
                })
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            AppRoot(
                modifier = Modifier.fillMaxSize(),
                innerPadding = innerPadding,
                viewModel = appRootViewModel,
                onOpenDrawer = {
                    scope.launch {
                        drawerState.open()
                    }
                }
            )

            AnimatedVisibility(
                visible = isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.LightGray.copy(alpha = 0.5f))
                        // Intercept all click events to disable click while loading
                        .clickable(
                            onClick = {},
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

@Composable
private fun Drawer(
    appVersion: String,
    onDeviceSettingsClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RectangleShape
    ) {
        NavigationDrawerItem(
            label = { Text(stringResource(R.string.device_settings)) },
            shape = RectangleShape,
            selected = false,
            onClick = onDeviceSettingsClick
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(stringResource(R.string.sign_out)) },
            shape = RectangleShape,
            selected = false,
            onClick = onSignOutClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = appVersion,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}