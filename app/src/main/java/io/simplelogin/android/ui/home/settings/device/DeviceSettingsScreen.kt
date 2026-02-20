package io.simplelogin.android.ui.home.settings.device

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.BrightnessAuto
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.LightMode
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
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.MailboxLite
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.preferences.Theme
import io.simplelogin.android.data.models.preferences.DefaultPrefix
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.ui.home.cell.AliasCell
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.OptionRow
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.ToggleOption
import io.simplelogin.android.ui.util.clickableRippleDisabled
import io.simplelogin.android.ui.util.primaryContentBackground
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsScreen(
    onDismiss: () -> Unit
) = with(hiltViewModel<DeviceSettingsViewModel>()) {
    val settings by deviceSettings.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
        LazyColumn(
            modifier =
                Modifier
                    .padding(horizontal = Spacing.regular)
                    .padding(bottom = Spacing.regular)
                    .padding(innerPadding)
        ) {
            item {
                OptionRow(
                    modifier = Modifier.primaryContentBackground(),
                    title = stringResource(R.string.theme),
                    description = { it.title(context = LocalContext.current) },
                    leadingIcon = {
                        val icon = when (it) {
                            Theme.LIGHT -> Icons.Outlined.LightMode
                            Theme.DARK -> Icons.Outlined.DarkMode
                            Theme.MATCH_SYSTEM -> Icons.Outlined.BrightnessAuto
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    },
                    options = Theme.entries.toTypedArray(),
                    selected = settings.theme,
                    onSelect = ::updateTheme
                )

                SettingsSpacer()
            }

            item {
                ToggleOption(
                    modifier = Modifier.primaryContentBackground(),
                    checked = settings.showStats,
                    onCheckedChange = ::updateShowStats,
                    title = stringResource(R.string.show_stats),
                    description = stringResource(R.string.show_stats_description)
                )

                SettingsSpacer()
            }

            item {
                Column(modifier = Modifier.primaryContentBackground()) {
                    DefaultPrefixSelection(
                        selected = settings.defaultPrefix,
                        onSelect = ::updateDefaultPrefix
                    )

                    AnimatedVisibility(visible = settings.defaultPrefix == DefaultPrefix.RANDOM_CHARACTERS) {
                        Column {
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
                }

                SettingsSpacer()
            }

            item {
                SettingsHeader(text = stringResource(R.string.alias_display_and_interaction))
                Column(modifier = Modifier.primaryContentBackground()) {
                    AliasCellSelectionSection(
                        modifier = Modifier.padding(bottom = Spacing.regular),
                        selected = settings.aliasCellSelection,
                        onSelect = ::updateAliasCellSelection
                    )

                    HorizontalDivider()

                    AliasOptionsDisplaySection(
                        modifier = Modifier.padding(vertical = Spacing.regular),
                        selected = settings.aliasOptionsDisplay,
                        onSelect = ::updateAliasOptionsDisplay
                    )

                    HorizontalDivider()

                    SwipeActionSelection(
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
        title = stringResource(R.string.alias_options_display),
        description = { it.title(LocalContext.current) },
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
        title = stringResource(R.string.select_alias_action),
        description = { it.title(LocalContext.current) },
        options = AliasCellSelection.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun SwipeActionSelection(
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
        modifier = Modifier.padding(vertical = Spacing.regular),
        title = stringResource(R.string.swipe_from_left_to_right),
        description = { it.title(LocalContext.current) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedLeftToRight,
        onSelect = onSelectLeftToRight
    )

    HorizontalDivider()

    OptionRow(
        modifier = Modifier.padding(vertical = Spacing.regular),
        title = stringResource(R.string.swipe_from_right_to_left),
        description = { it.title(LocalContext.current) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedRightToLeft,
        onSelect = onSelectRightToLeft
    )

    HorizontalDivider()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.regular)
            .clickableRippleDisabled { showAliasDisplayInfosDialog = true },
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
            .padding(top = Spacing.regular),
        text = stringResource(R.string.test_with_sample_alias),
        color = MaterialTheme.colorScheme.secondary,
        textAlign = TextAlign.Start
    )

    AliasCell(
        alias = Alias.sample,
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
        title = stringResource(R.string.default_prefix),
        description = { it.title(LocalContext.current) },
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