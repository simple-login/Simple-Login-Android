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
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.home.aliasactivities.AliasActivitiesScreen
import io.simplelogin.android.ui.home.aliascontacts.AliasContactsScreen
import io.simplelogin.android.ui.home.aliasdetail.AliasDetailPlaceholderScreen
import io.simplelogin.android.ui.home.aliasdetail.AliasDetailScreen
import io.simplelogin.android.ui.home.createalias.CreateAliasScreen
import io.simplelogin.android.ui.home.customdomains.CustomDomainDeletedAliasesScreen
import io.simplelogin.android.ui.home.customdomains.CustomDomainDetailsScreen
import io.simplelogin.android.ui.home.customdomains.CustomDomainsScreen
import io.simplelogin.android.ui.home.mailboxes.MailboxesScreen
import io.simplelogin.android.ui.home.settings.account.AccountSettingsScreen
import io.simplelogin.android.ui.home.settings.device.DeviceSettingsScreen
import io.simplelogin.android.ui.login.LoginMasterScreen
import kotlinx.serialization.Serializable

@Serializable
data object InitializationDestination : NavKey

@Serializable
data object LogInDestination : NavKey

@Serializable
// We pass API key as string instead ApiKey object because it's value class that has mangled suffixes
data class HomeDestination(val apiKey: String) : NavKey

@Serializable
data class CreateAliasDestination(val apiKey: String) : NavKey

@Serializable
data class AliasDetailsDestination(val alias: Alias, val apiKey: String) : NavKey

@Serializable
data class AliasContactsDestination(val alias: Alias, val apiKey: String) : NavKey

@Serializable
data class AliasActivitiesDestination(val alias: Alias, val apiKey: String) : NavKey

@Serializable
data object DeviceSettingsDestination : NavKey

@Serializable
data class AccountSettingsDestination(val apiKey: String) : NavKey

@Serializable
data class MailboxesDestination(val apiKey: String) : NavKey

@Serializable
data object CustomDomainsDestination : NavKey

@Serializable
data class CustomDomainDetailsDestination(val domain: CustomDomain) : NavKey

@Serializable
data class CustomDomainDeletedAliasesDestination(val domain: CustomDomain) : NavKey

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
    val accountSettingsDialogPayload by accountSettingsDialogPayload.collectAsState()
    val mailboxesDialogPayload by mailboxesDialogPayload.collectAsState()
    val showCustomDomainsDialog by showCustomDomainsDialog.collectAsState()
    val customDomainDetailsAsDialog by customDomainDetailsAsDialog.collectAsState()
    val customDomainDeletedAliasesAsDialog by customDomainDeletedAliasesAsDialog.collectAsState()
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
                    apiKeyValue = it.apiKey,
                    onOpenDrawer = onOpenDrawer,
                    onEnterSearch = viewModel::removeAliasDetails,
                    onViewDetails = ::viewAliasDetails,
                    onViewContacts = ::viewAliasContacts,
                    onCreateAlias = ::showCreateAliasScreen,
                    createdAliasFlow = viewModel.createdAlias
                )
            }

            entry<CreateAliasDestination> { key ->
                CreateAliasScreen(
                    apiKeyValue = key.apiKey,
                    onAliasCreated = viewModel::handleCreatedAlias,
                    onDismiss = viewModel::goBack
                )
            }

            entry<AliasDetailsDestination>(
                metadata = ListDetailSceneStrategy.detailPane()
            ) { key ->
                AliasDetailScreen(
                    alias = key.alias,
                    apiKeyValue = key.apiKey,
                    onGoBack = viewModel::goBack,
                    onViewContacts = { viewModel.viewAliasContacts(key.alias) },
                    onAliasUpdated = {
                        // TODO: Update alias list
                    },
                    onViewAllActivities = { viewModel.viewAliasActivities(key.alias) }
                )
            }

            entry<AliasContactsDestination>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) { key ->
                AliasContactsScreen(
                    alias = key.alias,
                    apiKeyValue = key.apiKey,
                    onGoBack = viewModel::goBack
                )
            }

            entry<AliasActivitiesDestination>(
                metadata = ListDetailSceneStrategy.extraPane()
            ) { key ->
                AliasActivitiesScreen(
                    alias = key.alias,
                    apiKeyValue = key.apiKey,
                    onGoBack = viewModel::goBack
                )
            }

            entry<DeviceSettingsDestination> {
                DeviceSettingsScreen(onDismiss = viewModel::goBack)
            }

            entry<AccountSettingsDestination> { key ->
                AccountSettingsScreen(
                    apiKeyValue = key.apiKey,
                    onDismiss = viewModel::goBack
                )
            }

            entry<MailboxesDestination> { key ->
                MailboxesScreen(
                    apiKeyValue = key.apiKey,
                    onDismiss = viewModel::goBack
                )
            }

            entry<CustomDomainsDestination> {
                CustomDomainsScreen(onViewDetails = {
                    viewModel.showCustomDomainDetails(
                        domain = it,
                        asDialog = false
                    )
                }, onDismiss = viewModel::goBack)
            }

            entry<CustomDomainDetailsDestination> {
                CustomDomainDetailsScreen(
                    domain = it.domain,
                    onDismiss = viewModel::goBack,
                    onViewDeletedAliases = {
                        viewModel.showCustomDomainDeletedAliases(
                            domain = it.domain,
                            asDialog = false
                        )
                    }
                )
            }

            entry<CustomDomainDeletedAliasesDestination> {
                CustomDomainDeletedAliasesScreen(domain = it.domain, onDismiss = viewModel::goBack)
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

    accountSettingsDialogPayload?.let { payload ->
        Dialog(onDismissRequest = ::dismissAccountSettingsDialog) {
            AccountSettingsScreen(
                apiKeyValue = payload.apiKey.value,
                onDismiss = ::dismissAccountSettingsDialog
            )
        }
    }

    mailboxesDialogPayload?.let { payload ->
        Dialog(onDismissRequest = ::dismissMailboxesDialog) {
            MailboxesScreen(
                apiKeyValue = payload.apiKey.value,
                onDismiss = ::dismissMailboxesDialog
            )
        }
    }

    if (showCustomDomainsDialog) {
        Dialog(onDismissRequest = ::dismissCustomDomainsDialog) {
            CustomDomainsScreen(
                onViewDetails = { showCustomDomainDetails(domain = it, asDialog = true) },
                onDismiss = ::dismissCustomDomainsDialog
            )
        }
    }

    customDomainDetailsAsDialog?.let {
        Dialog(onDismissRequest = ::dismissCustomDomainDetailsDialog) {
            CustomDomainDetailsScreen(
                domain = it,
                onDismiss = ::dismissCustomDomainDetailsDialog,
                onViewDeletedAliases = {
                    viewModel.showCustomDomainDeletedAliases(
                        domain = it,
                        asDialog = true
                    )
                })
        }
    }

    customDomainDeletedAliasesAsDialog?.let {
        Dialog(onDismissRequest = ::dismissCustomDomainDeletedAliasesDialog) {
            CustomDomainDeletedAliasesScreen(
                domain = it,
                onDismiss = ::dismissCustomDomainDeletedAliasesDialog
            )
        }
    }
}

fun WindowAdaptiveInfo.supportsMultiplePanes(): Boolean =
    windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
