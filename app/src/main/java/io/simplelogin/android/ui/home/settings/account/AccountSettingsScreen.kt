package io.simplelogin.android.ui.home.settings.account

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.RandomAliasSuffix
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.api.UsableDomain
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.OptionRow
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.ToggleOption
import io.simplelogin.android.ui.util.clickableRippleDisabled
import io.simplelogin.android.ui.util.primaryContentBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    onDismiss: () -> Unit
) = with(hiltViewModel<AccountSettingsViewModel>()) {
    val state by stateFlow.collectAsState()
    val settings = state.settings
    val fetchError = state.fetchError
    val updateError = state.updateError
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUsableDomainsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(updateError) {
        updateError?.let {
            snackbarHostState.showSnackbar(
                message = it.description(context)
            )
            clearUpdateError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.account_settings)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (settings != null) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = Spacing.regular)
                        .padding(bottom = Spacing.regular)
                ) {
                    accountSettingsScreenContent(
                        settings = settings,
                        onUpdateNotification = ::updateNotification,
                        onUpdateRandomMode = ::updateRandomMode,
                        onUpdateRandomAliasSuffix = ::updateRandomAliasSuffix,
                        onShowUsableDomainsSelector = { showUsableDomainsDialog = true }
                    )
                }
            }

            if (state.isLoading) {
                // Disable user's interaction when loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (fetchError != null) {
                RetryButton(error = fetchError, onRetry = ::refresh)
            }
        }
    }

    if (showUsableDomainsDialog) {
        state.settings?.let { settings ->
            UsableDomainsDialog(
                domains = settings.usableDomains,
                selected = settings.userSettings.randomAliasDefaultDomain,
                onSelect = {
                    showUsableDomainsDialog = false
                    updateUsableDomain(it)
                },
                onDismiss = { showUsableDomainsDialog = false }
            )
        }
    }
}

private fun LazyListScope.accountSettingsScreenContent(
    settings: AccountSettings,
    onUpdateNotification: (Boolean) -> Unit,
    onUpdateRandomMode: (RandomMode) -> Unit,
    onUpdateRandomAliasSuffix: (RandomAliasSuffix) -> Unit,
    onShowUsableDomainsSelector: () -> Unit
) {
    val userInfo = settings.userInfo
    val userSettings = settings.userSettings
    item {
        ToggleOption(
            modifier = Modifier.primaryContentBackground(),
            checked = userSettings.notification,
            onCheckedChange = onUpdateNotification,
            title = stringResource(R.string.newsletter),
            description = stringResource(R.string.newsletter_description)
        )
        SettingsSpacer()
    }

    item {
        SettingsHeader(text = stringResource(R.string.aliases))
        Column(modifier = Modifier.primaryContentBackground()) {
            OptionRow(
                modifier = Modifier.padding(bottom = Spacing.regular),
                title = stringResource(R.string.random_mode),
                description = { it.title(LocalContext.current) },
                options = RandomMode.entries.toTypedArray(),
                selected = userSettings.randomMode,
                onSelect = onUpdateRandomMode
            )

            HorizontalDivider()

            OptionRow(
                modifier = Modifier.padding(vertical = Spacing.regular),
                title = stringResource(R.string.random_suffix),
                description = { it.title(LocalContext.current) },
                options = RandomAliasSuffix.entries.toTypedArray(),
                selected = userSettings.randomAliasSuffix,
                onSelect = onUpdateRandomAliasSuffix
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.regular)
                    .clickableRippleDisabled(onClick = onShowUsableDomainsSelector),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(stringResource(R.string.default_domain_for_random_aliases))

                    Text(
                        text = userSettings.randomAliasDefaultDomain,
                        color = MaterialTheme.colorScheme.secondary,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Icon(imageVector = Icons.Default.Edit, contentDescription = null)
            }
        }
    }
}

@Composable
private fun UsableDomainsDialog(
    domains: List<UsableDomain>,
    selected: String,
    onSelect: (UsableDomain) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.default_domain_for_random_aliases)) },
        text = {
            LazyColumn {
                itemsIndexed(domains) { index, domain ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.medium)
                            .clickableRippleDisabled { onSelect(domain) },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                RadioButton(
                                    selected = domain.name == selected,
                                    onClick = { onSelect(domain) }
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = domain.name,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (domain.isCustom) {
                            Text(
                                text = stringResource(R.string.your_domain),
                                color = SlColor.Blue
                            )
                        }
                    }

                    if (index < domains.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {}
    )
}