package io.simplelogin.android.root

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
import io.simplelogin.android.home.HomeScreen
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.CustomDomain
import io.simplelogin.feature.accountsettings.AccountSettingsScreen
import io.simplelogin.feature.aliasactivities.AliasActivitiesScreen
import io.simplelogin.feature.aliascontacts.AliasContactsScreen
import io.simplelogin.feature.aliasdetail.AliasDetailPlaceholderScreen
import io.simplelogin.feature.aliasdetail.AliasDetailScreen
import io.simplelogin.feature.auth.ui.LoginMasterScreen
import io.simplelogin.feature.createalias.CreateAliasScreen
import io.simplelogin.feature.customdomains.CustomDomainDeletedAliasesScreen
import io.simplelogin.feature.customdomains.CustomDomainDetailsScreen
import io.simplelogin.feature.customdomains.CustomDomainsScreen
import io.simplelogin.feature.devicesettings.DeviceSettingsScreen
import io.simplelogin.feature.mailboxes.MailboxesScreen
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
data class CustomDomainsDestination(val apiKey: String) : NavKey

@Serializable
data class CustomDomainDetailsDestination(val domain: CustomDomain, val apiKey: String) : NavKey

@Serializable
data class CustomDomainDeletedAliasesDestination(val domain: CustomDomain, val apiKey: String) :
    NavKey

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
    val dialogStack by dialogStack.collectAsState()
    val activeDialog = dialogStack.lastOrNull()
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
        sceneStrategies = listOf(listDetailSceneStrategy),
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

            entry<CustomDomainsDestination> { key ->
                CustomDomainsScreen(
                    apiKeyValue = key.apiKey,
                    onViewDetails = {
                        viewModel.showCustomDomainDetails(
                            domain = it,
                            asDialog = false
                        )
                    },
                    onDismiss = viewModel::goBack
                )
            }

            entry<CustomDomainDetailsDestination> { key ->
                CustomDomainDetailsScreen(
                    domain = key.domain,
                    apiKeyValue = key.apiKey,
                    onDismiss = viewModel::goBack,
                    onViewDeletedAliases = {
                        viewModel.showCustomDomainDeletedAliases(
                            domain = key.domain,
                            asDialog = false
                        )
                    }
                )
            }

            entry<CustomDomainDeletedAliasesDestination> { key ->
                CustomDomainDeletedAliasesScreen(
                    domain = key.domain,
                    apiKeyValue = key.apiKey,
                    onDismiss = viewModel::goBack
                )
            }
        }
    )

    when (activeDialog) {
        AppRootDialog.LogOut -> AlertDialog(
            onDismissRequest = ::dismissActiveDialog,
            title = { Text(stringResource(R.string.sign_out)) },
            text = { Text(stringResource(R.string.sign_out_message)) },
            confirmButton = {
                TextButton(onClick = ::logOut) {
                    Text(stringResource(R.string.sign_out))
                }
            },
            dismissButton = {
                TextButton(onClick = ::dismissActiveDialog) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )

        AppRootDialog.DeviceSettings -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            DeviceSettingsScreen(onDismiss = ::dismissActiveDialog)
        }

        is AppRootDialog.AccountSettings -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            AccountSettingsScreen(
                apiKeyValue = activeDialog.apiKey.value,
                onDismiss = ::dismissActiveDialog
            )
        }

        is AppRootDialog.Mailboxes -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            MailboxesScreen(
                apiKeyValue = activeDialog.apiKey.value,
                onDismiss = ::dismissActiveDialog
            )
        }

        is AppRootDialog.CustomDomains -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            CustomDomainsScreen(
                apiKeyValue = activeDialog.apiKey.value,
                onViewDetails = { showCustomDomainDetails(domain = it, asDialog = true) },
                onDismiss = ::dismissActiveDialog
            )
        }

        is AppRootDialog.CustomDomainDetails -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            CustomDomainDetailsScreen(
                domain = activeDialog.domain,
                apiKeyValue = activeDialog.apiKey.value,
                onDismiss = ::dismissActiveDialog,
                onViewDeletedAliases = {
                    viewModel.showCustomDomainDeletedAliases(
                        domain = activeDialog.domain,
                        asDialog = true
                    )
                }
            )
        }

        is AppRootDialog.CustomDomainDeletedAliases -> Dialog(onDismissRequest = ::dismissActiveDialog) {
            CustomDomainDeletedAliasesScreen(
                domain = activeDialog.domain,
                apiKeyValue = activeDialog.apiKey.value,
                onDismiss = ::dismissActiveDialog
            )
        }

        null -> Unit
    }
}

fun WindowAdaptiveInfo.supportsMultiplePanes(): Boolean =
    windowSizeClass.isWidthAtLeastBreakpoint(WIDTH_DP_MEDIUM_LOWER_BOUND)
