package io.simplelogin.android

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ContactSupport
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.AllInbox
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.util.ProtonLinkManager
import io.simplelogin.android.util.ProtonLoginManager
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.preferences.Theme
import io.simplelogin.android.data.remote.BaseUrlProvider
import io.simplelogin.android.di.LoadingState
import io.simplelogin.android.di.LoadingStateFlow
import io.simplelogin.android.domain.snackbar.SnackbarManager
import io.simplelogin.android.domain.snackbar.colors
import io.simplelogin.android.ui.home.lockscreen.LockScreen
import io.simplelogin.android.ui.home.settings.account.UserInfoCard
import io.simplelogin.android.ui.root.AppRoot
import io.simplelogin.android.ui.root.AppRootViewModel
import io.simplelogin.android.ui.root.supportsMultiplePanes
import io.simplelogin.android.ui.theme.SimpleLoginTheme
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.clickableRippleDisabled
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.settings.ObserveDeviceSettingsUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private val appRootViewModel: AppRootViewModel by viewModels()

    @Inject
    lateinit var protonLoginManager: ProtonLoginManager

    @Inject
    lateinit var protonLinkManager: ProtonLinkManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            setRecentsScreenshotEnabled(false)
        }
        viewModel.observe()
        setUpSplashScreen()
        applyOrientationRestrictions()
        enableEdgeToEdge()
        setContent {
            val devicePreferences by viewModel.devicePreferences.collectAsState()
            val darkTheme = when (devicePreferences.theme) {
                Theme.LIGHT -> false
                Theme.DARK -> true
                Theme.MATCH_SYSTEM -> isSystemInDarkTheme()
            }
            val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            val scope = rememberCoroutineScope()
            val windowAdaptiveInfo = currentWindowAdaptiveInfo()
            val asDialog = windowAdaptiveInfo.supportsMultiplePanes()
            val appRooState by appRootViewModel.stateFlow.collectAsState()
            val userInfo by viewModel.userInfoStateFlow.collectAsState()

            DisposableEffect(darkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme },
                    navigationBarStyle = SystemBarStyle.auto(
                        android.graphics.Color.TRANSPARENT,
                        android.graphics.Color.TRANSPARENT,
                    ) { darkTheme }
                )
                onDispose {}
            }

            fun closeDrawerAndExecute(task: () -> Unit) {
                scope.launch {
                    drawerState.close()
                    task()
                }
            }

            fun openAccountSettings() {
                closeDrawerAndExecute {
                    appRootViewModel.showAccountSettingsScreen(asDialog)
                }
            }

            SimpleLoginTheme(darkTheme = darkTheme, dynamicColor = devicePreferences.dynamicColor) {
                Box(modifier = Modifier.fillMaxSize()) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = userInfo != null,
                        drawerContent = {
                            Drawer(
                                appVersion = appRootViewModel.appVersion,
                                userInfo = userInfo,
                                onUserInfoClick = {
                                    openAccountSettings()
                                },
                                onMailboxesClick = {
                                    closeDrawerAndExecute {
                                        appRootViewModel.showMailboxesScreen(asDialog)
                                    }
                                },
                                onCustomDomainsClick = {
                                    closeDrawerAndExecute {
                                        appRootViewModel.showCustomDomainsScreen(asDialog)
                                    }
                                },
                                onAccountSettingsClick = {
                                    openAccountSettings()
                                },
                                onDeviceSettingsClick = {
                                    closeDrawerAndExecute {
                                        appRootViewModel.showDeviceSettingsScreen(asDialog)
                                    }
                                },
                                onContactUsClick = {
                                    closeDrawerAndExecute(::openContactUsPage)
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

                    if (appRooState.apiKey != null) {
                        LockScreen(onLogOut = { appRootViewModel.logOut() })
                    }
                }
            }
        }
    }

    /**
     * Callback for when the Login with Proton process is done.
     * The Login with Proton will redirect the user to
     * auth.simplelogin://**/login?apikey=YOUR_API_KEY
     *
     * (The intent-filter is registered in AndroidManifest.xml)
     */
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        when (intent.data?.path) {
            "/login" -> {
                val apiKey = intent.data?.getQueryParameter("apikey") ?: return
                protonLoginManager.pendingApiKey.tryEmit(apiKey)
            }

            "/link" -> protonLinkManager.linkedEvents.tryEmit(Unit)
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

    private fun openContactUsPage() {
        val intent = Intent(Intent.ACTION_VIEW, "https://simplelogin.io/contact/".toUri())
        startActivity(intent)
    }
}

// TODO: Convert to a use case
@HiltViewModel
class MainViewModel @Inject constructor(
    @LoadingState private val loadingState: LoadingStateFlow,
    private val baseUrlProvider: BaseUrlProvider,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    val snackbarManager: SnackbarManager,
    observeDeviceSettings: ObserveDeviceSettingsUseCase
) : ViewModel() {
    val showLoadingIndicator = loadingState.asStateFlow()
    val devicePreferences = observeDeviceSettings()
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            DevicePreferences.Default
        )

    val userInfoStateFlow = observeSessionSettings()
        .map { it.userInfo }
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            null
        )

    fun observe() {
        viewModelScope.launch {
            observeSessionSettings()
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
                        .background(Color.LightGray.copy(alpha = 0.1f))
                        // Intercept all click events to disable click while loading
                        .clickableRippleDisabled(onClick = {}),
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
    userInfo: UserInfo?,
    onUserInfoClick: () -> Unit,
    onMailboxesClick: () -> Unit,
    onCustomDomainsClick: () -> Unit,
    onAccountSettingsClick: () -> Unit,
    onDeviceSettingsClick: () -> Unit,
    onContactUsClick: () -> Unit,
    onSignOutClick: () -> Unit
) {
    ModalDrawerSheet(
        drawerShape = RectangleShape
    ) {
        userInfo?.let {
            UserInfoCard(userInfo = it, onClick = onUserInfoClick)
        }

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.mailboxes)) },
            icon = { Icon(imageVector = Icons.Outlined.AllInbox, contentDescription = null) },
            shape = RectangleShape,
            selected = false,
            onClick = onMailboxesClick
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.custom_domains)) },
            icon = { Icon(imageVector = Icons.Outlined.Language, contentDescription = null) },
            shape = RectangleShape,
            selected = false,
            onClick = onCustomDomainsClick
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.account_settings)) },
            icon = { Icon(imageVector = Icons.Outlined.Person, contentDescription = null) },
            shape = RectangleShape,
            selected = false,
            onClick = onAccountSettingsClick
        )

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.device_settings)) },
            icon = { Icon(imageVector = Icons.Outlined.Settings, contentDescription = null) },
            shape = RectangleShape,
            selected = false,
            onClick = onDeviceSettingsClick
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.contact_us)) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ContactSupport,
                    contentDescription = null
                )
            },
            shape = RectangleShape,
            selected = false,
            onClick = onContactUsClick
        )

        HorizontalDivider()

        NavigationDrawerItem(
            label = { Text(text = stringResource(R.string.sign_out)) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.Logout,
                    contentDescription = null
                )
            },
            shape = RectangleShape,
            selected = false,
            onClick = onSignOutClick
        )

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier.widthIn(max = 180.dp),
                painter = painterResource(R.drawable.ic_logo_powered_by_proton),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null
            )
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = appVersion,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}