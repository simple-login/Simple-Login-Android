package io.simplelogin.android.ui.home.settings.device

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Brightness6
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.MailboxLite
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.preferences.DefaultPrefix
import io.simplelogin.android.data.models.preferences.DeviceLockType
import io.simplelogin.android.data.models.preferences.DevicePreferences
import io.simplelogin.android.data.models.preferences.LockTimeOut
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.data.models.preferences.Theme
import io.simplelogin.android.data.models.preferences.UserSessionPreferences
import io.simplelogin.android.ui.home.cell.AliasCell
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.OptionRow
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.ToggleOption
import io.simplelogin.android.ui.util.primaryContentBackground
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsScreen(
    viewModel: DeviceSettingsViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    val state by viewModel.stateFlow.collectAsState()

    Scaffold(
        containerColor = SlColor.BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_settings)) },
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
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is DeviceSettingsState.Loading ->
                    CircularProgressIndicator()

                is DeviceSettingsState.Loaded ->
                    DeviceSettingsContent(
                        viewModel = viewModel,
                        session = (state as DeviceSettingsState.Loaded).session,
                        settings = (state as DeviceSettingsState.Loaded).settings
                    )
            }
        }
    }
}

@Composable
private fun DeviceSettingsContent(
    session: UserSessionPreferences,
    settings: DevicePreferences,
    viewModel: DeviceSettingsViewModel
) = with(viewModel) {
    var showSetPinDialog by rememberSaveable { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier
            .padding(horizontal = Spacing.regular)
            .padding(bottom = Spacing.regular)
    ) {
        item {
            SettingsHeader(text = stringResource(R.string.security))

            Column(modifier = Modifier.primaryContentBackground()) {
                OptionRow(
                    paddingValues = PaddingValues(Spacing.regular),
                    title = stringResource(R.string.unlock_with),
                    description = { Text(text = it.title(context = LocalContext.current)) },
                    options = DeviceLockType.entries.toTypedArray(),
                    selected = session.lockType,
                    onSelect = {
                        when (it) {
                            DeviceLockType.NONE -> {}
                            DeviceLockType.BIOMETRIC -> {}
                            DeviceLockType.PIN -> showSetPinDialog = true
                        }
                    }
                )

                AnimatedVisibility(visible = session.lockType != DeviceLockType.NONE) {
                    Column {
                        HorizontalDivider()

                        OptionRow(
                            paddingValues = PaddingValues(Spacing.regular),
                            title = stringResource(R.string.automatic_lock),
                            description = { Text(text = it.title(context = LocalContext.current)) },
                            options = LockTimeOut.entries.toTypedArray(),
                            selected = session.lockTimeOut,
                            onSelect = ::updateLockTimeout
                        )
                    }
                }

                AnimatedVisibility(visible = session.lockType == DeviceLockType.PIN) {
                    Column {
                        HorizontalDivider()

                        Text(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {}
                                .padding(Spacing.regular),
                            text = stringResource(R.string.change_pin_code),
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.display))

            Column(modifier = Modifier.primaryContentBackground()) {
                OptionRow(
                    paddingValues = PaddingValues(Spacing.regular),
                    title = stringResource(R.string.theme),
                    description = {
                        val icon = when (it) {
                            Theme.LIGHT -> Icons.Outlined.LightMode
                            Theme.DARK -> Icons.Outlined.DarkMode
                            Theme.MATCH_SYSTEM -> Icons.Outlined.Brightness6
                        }
                        Row {
                            Icon(imageVector = icon, contentDescription = null)
                            Spacer(modifier = Modifier.width(Spacing.medium))
                            Text(text = it.title(context = LocalContext.current))
                        }
                    },
                    options = Theme.entries.toTypedArray(),
                    selected = settings.theme,
                    onSelect = ::updateTheme
                )

                HorizontalDivider()

                ToggleOption(
                    paddingValues = PaddingValues(Spacing.regular),
                    checked = settings.showStats,
                    onCheckedChange = ::updateShowStats,
                    title = stringResource(R.string.show_stats),
                    description = stringResource(R.string.show_stats_description)
                )
            }

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.alias_creation))

            Column(modifier = Modifier.primaryContentBackground()) {
                DefaultPrefixSelection(
                    selected = settings.defaultPrefix,
                    onSelect = ::updateDefaultPrefix
                )

                AnimatedVisibility(visible = settings.defaultPrefix == DefaultPrefix.RANDOM_CHARACTERS) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = Spacing.regular)
                            .padding(bottom = Spacing.regular)
                    ) {
                        Text(
                            text = stringResource(R.string.number_of_random_characters),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Start
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Slider(
                                modifier = Modifier.weight(1f),
                                value = settings.prefixRandomCharacterCount.toFloat(),
                                onValueChange = { updateRandomCharacterCount(it.toInt()) },
                                valueRange = 1f..10f,
                                steps = 8
                            )

                            Text(
                                modifier = Modifier.padding(start = Spacing.mediumLarge),
                                text = "${settings.prefixRandomCharacterCount}"
                            )
                        }
                    }
                }

                HorizontalDivider()

                ToggleOption(
                    paddingValues = PaddingValues(Spacing.regular),
                    checked = settings.copyAfterCreating,
                    onCheckedChange = ::updateCopyAfterCreating,
                    title = stringResource(R.string.copy_after_creating)
                )

                HorizontalDivider()

                ToggleOption(
                    paddingValues = PaddingValues(Spacing.regular),
                    checked = settings.askForRandomAliasNote,
                    onCheckedChange = ::updateAskForRandomAliasNote,
                    title = stringResource(R.string.ask_for_random_alias_note),
                    description = stringResource(R.string.ask_for_random_alias_note_description)
                )
            }

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.alias_display_and_interaction))
            Column(modifier = Modifier.primaryContentBackground()) {
                AliasCellSelectionSection(
                    modifier = Modifier,
                    selected = settings.aliasCellSelection,
                    onSelect = ::updateAliasCellSelection
                )

                HorizontalDivider()

                AliasOptionsDisplaySection(
                    modifier = Modifier,
                    selected = settings.aliasOptionsDisplay,
                    onSelect = ::updateAliasOptionsDisplay
                )

                HorizontalDivider()

                SwipeActionSelection(
                    selectedAliasCellSelection = settings.aliasCellSelection,
                    selectedOptionsDisplay = settings.aliasOptionsDisplay,
                    selectedLeftToRight = settings.swipeFromLeftToRightAction,
                    onSelectLeftToRight = ::updateSwipeFromLeftToRight,
                    selectedRightToLeft = settings.swipeFromRightToLeftAction,
                    onSelectRightToLeft = ::updateSwipeFromRightToLeft,
                    selectedAliasDisplayInfos = settings.aliasDisplayInfos,
                    onSaveAliasDisplayInfos = ::updateAliasDisplayInfos
                )
            }
        }
    }

    if (showSetPinDialog) {
        CreateOrConfirmPinDialog(
            mode = CreateOrEditPinMode.CREATE,
            onConfirm = {},
            onDismiss = { showSetPinDialog = false })
    }
}

@Composable
private fun AliasOptionsDisplaySection(
    modifier: Modifier,
    selected: AliasOptionsDisplay,
    onSelect: (AliasOptionsDisplay) -> Unit
) {
    OptionRow(
        modifier = modifier,
        paddingValues = PaddingValues(Spacing.regular),
        title = stringResource(R.string.alias_options_display),
        description = { Text(text = it.title(LocalContext.current)) },
        options = AliasOptionsDisplay.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun AliasCellSelectionSection(
    modifier: Modifier,
    selected: AliasCellSelection,
    onSelect: (AliasCellSelection) -> Unit
) {
    OptionRow(
        modifier = modifier,
        paddingValues = PaddingValues(Spacing.regular),
        title = stringResource(R.string.select_alias_action),
        description = { Text(text = it.title(LocalContext.current)) },
        options = AliasCellSelection.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun SwipeActionSelection(
    selectedAliasCellSelection: AliasCellSelection,
    selectedOptionsDisplay: AliasOptionsDisplay,
    selectedLeftToRight: SwipeAction,
    onSelectLeftToRight: (SwipeAction) -> Unit,
    selectedRightToLeft: SwipeAction,
    onSelectRightToLeft: (SwipeAction) -> Unit,
    selectedAliasDisplayInfos: List<AliasDisplayInfo>,
    onSaveAliasDisplayInfos: (List<AliasDisplayInfo>) -> Unit,
) {
    val context = LocalContext.current
    var showAliasDisplayInfosDialog by rememberSaveable { mutableStateOf(false) }

    OptionRow(
        paddingValues = PaddingValues(Spacing.regular),
        title = stringResource(R.string.swipe_from_left_to_right),
        description = { Text(text = it.title(context)) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedLeftToRight,
        onSelect = onSelectLeftToRight
    )

    HorizontalDivider()

    OptionRow(
        paddingValues = PaddingValues(Spacing.regular),
        title = stringResource(R.string.swipe_from_right_to_left),
        description = { Text(text = it.title(context)) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedRightToLeft,
        onSelect = onSelectRightToLeft
    )

    HorizontalDivider()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showAliasDisplayInfosDialog = true }
            .padding(Spacing.regular),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.alias_information))

            Text(
                text = if (selectedAliasDisplayInfos.isEmpty()) {
                    stringResource(R.string.alias_address_only)
                } else if (selectedAliasDisplayInfos.size == AliasDisplayInfo.entries.size) {
                    stringResource(R.string.all_information)
                } else {
                    selectedAliasDisplayInfos.joinToString(", ") { it.title(context) }
                },
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        Icon(imageVector = Icons.Default.Edit, contentDescription = null)
    }

    HorizontalDivider()

    Text(
        modifier = Modifier
            .padding(top = Spacing.regular)
            .padding(horizontal = Spacing.regular),
        text = stringResource(R.string.test_with_sample_alias),
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Start
    )

    AliasCell(
        alias = Alias.sample,
        cellSelection = selectedAliasCellSelection,
        optionsDisplay = selectedOptionsDisplay,
        displayInfos = selectedAliasDisplayInfos,
        swipeFromStartToEndAction = selectedLeftToRight,
        swipeFromEndToStartAction = selectedRightToLeft,
        onAction = null
    )

    if (showAliasDisplayInfosDialog) {
        AliasDisplayInfosDialog(
            selection = selectedAliasDisplayInfos,
            onSelectionChange = onSaveAliasDisplayInfos,
            onDismiss = { showAliasDisplayInfosDialog = false }
        )
    }
}

@Composable
private fun DefaultPrefixSelection(
    modifier: Modifier = Modifier,
    selected: DefaultPrefix,
    onSelect: (DefaultPrefix) -> Unit
) {
    OptionRow(
        modifier = modifier,
        paddingValues = PaddingValues(
            start = Spacing.regular,
            end = Spacing.regular,
            top = Spacing.regular,
            bottom = if (selected != DefaultPrefix.RANDOM_CHARACTERS) Spacing.regular else Spacing.small
        ),
        title = stringResource(R.string.default_prefix),
        description = { Text(text = it.title(LocalContext.current)) },
        options = DefaultPrefix.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@OptIn(ExperimentalTime::class)
private val Alias.Companion.sample: Alias
    get() = Alias(
        id = AliasId(value = 0),
        email = "newsletter@simplelogin.io",
        name = null,
        enabled = true,
        creationTimestamp = Clock.System.now().minus(10.days).epochSeconds.toDouble(),
        blockCount = 56,
        forwardCount = 90,
        replyCount = 15,
        note = "Tech newsletter",
        pgpSupported = false,
        pgpDisabled = false,
        mailboxes = listOf(
            MailboxLite(id = 0, email = "john.doe@protonmail.com"),
            MailboxLite(id = 1, email = "jane.doe@pm.me")
        ),
        latestActivity = Alias.LatestActivity(
            action = ActivityAction.REPLY,
            contact = Alias.LatestActivity.Contact(
                email = "eric.norbert@proton.me",
                name = null,
                reverseAlias = ""
            ),
            timestamp = Clock.System.now().minus(5.minutes).epochSeconds.toDouble()
        ),
        pinned = true
    )