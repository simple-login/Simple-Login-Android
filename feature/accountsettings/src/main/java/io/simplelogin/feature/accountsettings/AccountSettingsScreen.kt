package io.simplelogin.feature.accountsettings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.core.common.timeAndFullDate
import io.simplelogin.core.designsystem.OptionRow
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.SettingsFooter
import io.simplelogin.core.designsystem.SettingsHeader
import io.simplelogin.core.designsystem.SettingsSpacer
import io.simplelogin.core.designsystem.ToggleOption
import io.simplelogin.core.designsystem.clickableRippleDisabled
import io.simplelogin.core.designsystem.description
import io.simplelogin.core.designsystem.primaryContentBackground
import io.simplelogin.core.designsystem.theme.ProtonPurple
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.designsystem.title
import io.simplelogin.core.model.api.RandomAliasSuffix
import io.simplelogin.core.model.api.RandomMode
import io.simplelogin.core.model.api.SenderFormat
import io.simplelogin.core.model.api.UsableDomain
import io.simplelogin.core.ui.EditTextDialog
import io.simplelogin.core.ui.UserInfoCard
import io.simplelogin.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    apiKeyValue: String,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel { factory: AccountSettingsViewModel.Factory ->
        factory.create(apiKeyValue)
    }

    val state by viewModel.stateFlow.collectAsState()
    val settings = state.settings
    val fetchError = state.fetchError
    val updateError = state.updateError
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showUnlinkProtonDialog by remember { mutableStateOf(false) }
    var showUsableDomainsDialog by remember { mutableStateOf(false) }
    var showEditUserInfoMenu by remember { mutableStateOf(false) }
    var showEditDisplayNameDialog by remember { mutableStateOf(false) }

    val information by viewModel.informationStateFlow.collectAsState()

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { viewModel.updateProfilePicture(uri = it, context = context) }
    )

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    LaunchedEffect(updateError) {
        updateError?.let {
            snackbarHostState.showSnackbar(message = it.description(context))
            viewModel.clearUpdateError()
        }
    }

    LaunchedEffect(information) {
        information?.let {
            snackbarHostState.showSnackbar(message = it)
            viewModel.clearInformation()
        }
    }

    Scaffold(
        containerColor = SlColor.BackgroundColor,
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
                        showEditUserInfoMenu = showEditUserInfoMenu,
                        onShowEditUserInfoMenu = { showEditUserInfoMenu = true },
                        onDismissEditUserInfoMenu = { showEditUserInfoMenu = false },
                        onEditDisplayName = {
                            showEditUserInfoMenu = false
                            showEditDisplayNameDialog = true
                        },
                        onEditProfilePicture = {
                            showEditUserInfoMenu = false
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                        onRemoveProfilePicture = {
                            showEditUserInfoMenu = false
                            viewModel.removeProfilePicture()
                        },
                        onLinkProton = viewModel::linkProton,
                        onUnlinkProton = { showUnlinkProtonDialog = true },
                        onUpdateNotification = viewModel::updateNotification,
                        onUpdateRandomMode = viewModel::updateRandomMode,
                        onUpdateRandomAliasSuffix = viewModel::updateRandomAliasSuffix,
                        onShowUsableDomainsSelector = { showUsableDomainsDialog = true },
                        onUpdateSenderFormat = viewModel::updateSenderFormat
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
                RetryButton(error = fetchError, onRetry = viewModel::refresh)
            }
        }
    }

    if (showUnlinkProtonDialog) {
        state.settings?.userInfo?.connectedProtonAddress?.let {
            AlertDialog(
                onDismissRequest = { showUnlinkProtonDialog = false },
                title = { Text(text = stringResource(R.string.unlink_proton)) },
                text = {
                    Text(text = stringResource(R.string.unlink_proton_description, it))
                },
                confirmButton = {
                    TextButton(onClick = {
                        showUnlinkProtonDialog = false
                        viewModel.unlinkProton()
                    }) {
                        Text(text = stringResource(R.string.yes_unlink_proton_account))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showUnlinkProtonDialog = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    if (showUsableDomainsDialog) {
        state.settings?.let { settings ->
            UsableDomainsDialog(
                domains = settings.usableDomains,
                selected = settings.userSettings.randomAliasDefaultDomain,
                onSelect = {
                    showUsableDomainsDialog = false
                    viewModel.updateUsableDomain(it)
                },
                onDismiss = { showUsableDomainsDialog = false }
            )
        }
    }

    if (showEditDisplayNameDialog) {
        EditTextDialog(
            initialValue = state.settings?.userInfo?.name,
            title = stringResource(R.string.edit_display_name),
            onSave = {
                showEditDisplayNameDialog = false
                viewModel.updateDisplayName(it)
            },
            onDismiss = { showEditDisplayNameDialog = false }
        )
    }
}

private fun LazyListScope.accountSettingsScreenContent(
    settings: AccountSettings,
    showEditUserInfoMenu: Boolean,
    onShowEditUserInfoMenu: () -> Unit,
    onDismissEditUserInfoMenu: () -> Unit,
    onEditDisplayName: () -> Unit,
    onEditProfilePicture: () -> Unit,
    onRemoveProfilePicture: () -> Unit,
    onLinkProton: () -> Unit,
    onUnlinkProton: () -> Unit,
    onUpdateNotification: (Boolean) -> Unit,
    onUpdateRandomMode: (RandomMode) -> Unit,
    onUpdateRandomAliasSuffix: (RandomAliasSuffix) -> Unit,
    onShowUsableDomainsSelector: () -> Unit,
    onUpdateSenderFormat: (SenderFormat) -> Unit,
) {
    val userInfo = settings.userInfo
    val connectedProtonAddress = userInfo.connectedProtonAddress
    val userSettings = settings.userSettings

    item {
        UserInfoCard(
            modifier = Modifier.primaryContentBackground(),
            userInfo = userInfo,
            onClick = onShowEditUserInfoMenu,
            editMenu = {
                Box {
                    IconButton(onClick = onShowEditUserInfoMenu) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = stringResource(R.string.edit_profile)
                        )
                    }

                    DropdownMenu(
                        expanded = showEditUserInfoMenu,
                        onDismissRequest = onDismissEditUserInfoMenu
                    ) {
                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.edit_display_name)) },
                            onClick = onEditDisplayName
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.edit_profile_picture)) },
                            onClick = onEditProfilePicture
                        )

                        HorizontalDivider()

                        DropdownMenuItem(
                            text = { Text(text = stringResource(R.string.remove_profile_picture)) },
                            onClick = onRemoveProfilePicture
                        )
                    }
                }
            }
        )

        userInfo.trialEndTimestamp?.let {
            SettingsFooter(text = stringResource(R.string.trial_end_date, it.timeAndFullDate()))
            SettingsFooter(text = stringResource(R.string.trial_description))
        }

        SettingsSpacer()
    }

    item {
        SettingsHeader(text = "Proton")

        Row(
            modifier = Modifier
                .primaryContentBackground()
                .clickable(onClick = {
                    if (connectedProtonAddress != null) {
                        onUnlinkProton()
                    } else {
                        onLinkProton()
                    }
                })
                .padding(Spacing.regular),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(DesignSystemR.drawable.ic_proton),
                tint = Color.Unspecified,
                contentDescription = null
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = Spacing.medium),
                text = if (connectedProtonAddress != null) {
                    stringResource(R.string.unlink_proton)
                } else {
                    stringResource(R.string.link_with_proton)
                },
                color = ProtonPurple
            )
        }

        Text(
            modifier = Modifier
                .padding(top = Spacing.small)
                .padding(horizontal = Spacing.regular),
            text = if (connectedProtonAddress != null) {
                buildAnnotatedString {
                    append(stringResource(R.string.already_linked_to_proton))
                    append(" ")
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(connectedProtonAddress)
                    }
                }
            } else {
                AnnotatedString(stringResource(R.string.not_linked_to_proton))
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )

        SettingsSpacer()
    }

    item {
        ToggleOption(
            modifier = Modifier.primaryContentBackground(),
            paddingValues = PaddingValues(Spacing.regular),
            checked = userSettings.notification,
            onCheckedChange = onUpdateNotification,
            title = stringResource(R.string.newsletter),
            description = stringResource(R.string.newsletter_description)
        )

        SettingsSpacer()
    }

    item {
        SettingsHeader(text = stringResource(R.string.random_aliases))

        Column(modifier = Modifier.primaryContentBackground()) {
            OptionRow(
                paddingValues = PaddingValues(Spacing.regular),
                title = stringResource(R.string.random_mode),
                description = { Text(text = it.title(LocalContext.current)) },
                options = RandomMode.entries.toTypedArray(),
                selected = userSettings.randomMode,
                onSelect = onUpdateRandomMode
            )

            HorizontalDivider()

            OptionRow(
                paddingValues = PaddingValues(Spacing.regular),
                title = stringResource(R.string.random_suffix),
                description = { Text(text = it.title(LocalContext.current)) },
                options = RandomAliasSuffix.entries.toTypedArray(),
                selected = userSettings.randomAliasSuffix,
                onSelect = onUpdateRandomAliasSuffix
            )

            HorizontalDivider()

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onShowUsableDomainsSelector)
                    .padding(Spacing.regular),
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

        SettingsSpacer()
    }

    item {
        OptionRow(
            modifier = Modifier.primaryContentBackground(),
            paddingValues = PaddingValues(Spacing.regular),
            title = stringResource(R.string.sender_address_format),
            description = { Text(text = it.description(LocalContext.current)) },
            options = SenderFormat.entries.toTypedArray(),
            selected = userSettings.senderFormat,
            onSelect = onUpdateSenderFormat
        )
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
