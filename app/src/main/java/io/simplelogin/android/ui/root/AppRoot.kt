package io.simplelogin.android.ui.root

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.home.settings.DeviceSettingsScreen
import io.simplelogin.android.ui.home.HomeScreen
import io.simplelogin.android.ui.home.aliasdetail.AliasDetailScreen
import io.simplelogin.android.ui.login.LoginMasterScreen
import io.simplelogin.android.ui.nav.TwoPaneScene
import io.simplelogin.android.ui.nav.rememberTwoPaneSceneStrategy
import kotlinx.serialization.Serializable

@Serializable
data object InitializationDestination: NavKey

@Serializable
data object LogInDestination: NavKey

@Serializable
data object HomeDestination: NavKey

@Serializable
data class AliasDetails(val alias: Alias): NavKey

@Serializable
data class AliasContacts(val alias: Alias): NavKey

@Serializable
data object DeviceSettingsDestination: NavKey

@Composable
fun AppRoot(modifier: Modifier = Modifier,
            innerPadding: PaddingValues,
            viewModel: AppRootViewModel,
            onOpenDrawer: () -> Unit
) = with(viewModel) {
    val backStack by navBackStack.collectAsState()

    val showLogOutDialog by showLogOutDialog.collectAsState()
    val twoPaneStrategy = rememberTwoPaneSceneStrategy<NavKey>()

    NavDisplay(
        modifier = modifier,
        backStack = backStack,
        sceneStrategy = twoPaneStrategy,
        entryProvider = entryProvider {
            entry<InitializationDestination> {}

            entry<LogInDestination> {
                LoginMasterScreen(modifier = modifier.padding(innerPadding))
            }

            entry<HomeDestination>(
                metadata = TwoPaneScene.permanentPane()
            ) {
                HomeScreen(
                    modifier = modifier,
                    onOpenDrawer = onOpenDrawer,
                    onViewDetails = ::viewAliasDetails,
                    onViewContacts = ::viewAliasContacts
                )
            }

            entry<AliasDetails>(
                metadata = TwoPaneScene.detailPane()
            ) { key ->
                AliasDetailScreen(alias = key.alias)
            }

            entry<AliasContacts>(
                metadata = TwoPaneScene.detailPane()
            ) { key ->
                Text(
                    modifier = modifier,
                    text = "Alias contacts ${key.alias.email}"
                )
            }

            entry<DeviceSettingsDestination> {
                DeviceSettingsScreen()
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
}

