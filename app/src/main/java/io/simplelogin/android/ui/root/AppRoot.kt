package io.simplelogin.android.ui.root

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.WindowAdaptiveInfo
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.material3.adaptive.layout.calculatePaneScaffoldDirective
import androidx.compose.material3.adaptive.navigation3.ListDetailSceneStrategy
import androidx.compose.material3.adaptive.navigation3.rememberListDetailSceneStrategy
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.window.core.layout.WindowSizeClass.Companion.WIDTH_DP_MEDIUM_LOWER_BOUND
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.home.settings.DeviceSettingsScreen
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.home.aliascontacts.AliasContactsScreen
import io.simplelogin.android.ui.home.aliasdetail.AliasDetailPlaceholderScreen
import io.simplelogin.android.ui.home.aliasdetail.AliasDetailScreen
import io.simplelogin.android.ui.home.createalias.CreateAliasScreen
import io.simplelogin.android.ui.login.LoginMasterScreen
import kotlinx.serialization.Serializable

@Serializable
data object InitializationDestination : NavKey

@Serializable
data object LogInDestination : NavKey

@Serializable
data object HomeDestination : NavKey

@Serializable
data object CreateAliasDestination : NavKey

@Serializable
data class AliasDetailsDestination(val alias: Alias) : NavKey

@Serializable
data class AliasContactsDestination(val alias: Alias) : NavKey

@Serializable
data object DeviceSettingsDestination : NavKey

@SuppressLint("ConfigurationScreenWidthHeight")
@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun AppRoot(
    modifier: Modifier = Modifier,
    innerPadding: PaddingValues,
    viewModel: AppRootViewModel,
    onOpenDrawer: () -> Unit
) = with(viewModel) {
    val backStack by navBackStack.collectAsState()

    val showLogOutDialog by showLogOutDialog.collectAsState()
    val showDeviceSettingsDialog by showDeviceSettingsDialog.collectAsState()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    val listPaneWidth = if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        screenWidth * 0.4f
    } else {
        screenWidth * 0.5f
    }
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()
    val listDetailSceneStrategy = rememberListDetailSceneStrategy<NavKey>(
        directive = calculatePaneScaffoldDirective(windowAdaptiveInfo).copy(
            defaultPanePreferredWidth = listPaneWidth,
            maxHorizontalPartitions = if (windowAdaptiveInfo.supportsMultiplePanes()) 2 else 1,
            horizontalPartitionSpacerSize = 0.dp
        )
    )

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = listDetailSceneStrategy,
        entryProvider = entryProvider {
            entry<InitializationDestination> {}

            entry<LogInDestination> {
                LoginMasterScreen(modifier = modifier.padding(innerPadding))
            }

            entry<HomeDestination>(
                metadata = ListDetailSceneStrategy.listPane(detailPlaceholder = { AliasDetailPlaceholderScreen() })
            ) {
                HomeScreen(
                    modifier = modifier,
                    onOpenDrawer = onOpenDrawer,
                    onViewDetails = ::viewAliasDetails,
                    onViewContacts = ::viewAliasContacts,
                    onCreateAlias = ::showCreateAliasScreen
                )
            }

            entry<CreateAliasDestination> {
                CreateAliasScreen(onDismiss = viewModel::goBack)
            }

            entry<AliasDetailsDestination>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                AliasDetailScreen(
                    alias = key.alias,
                    onGoBack = viewModel::goBack,
                    onViewContacts = { viewModel.viewAliasContacts(key.alias) }
                )
            }

            entry<AliasContactsDestination>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) { key ->
                AliasContactsScreen(
                    alias = key.alias,
                    onGoBack = viewModel::goBack
                )
            }

            entry<DeviceSettingsDestination> {
                DeviceSettingsScreen(onDismiss = viewModel::goBack)
            }
        }
    )

    if (showLogOutDialog) {
        AlertDialog(
            onDismissRequest = ::dismissLogOutDialog,
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_message)) },
            confirmButton = {
                TextButton(onClick = ::logOut) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissLogOutDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDeviceSettingsDialog) {
        Dialog(onDismissRequest = ::dismissDeviceSettingsDialog) {
            DeviceSettingsScreen(onDismiss = ::dismissDeviceSettingsDialog)
        }
    }
}

fun WindowAdaptiveInfo.supportsMultiplePanes(): Boolean =
    windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)