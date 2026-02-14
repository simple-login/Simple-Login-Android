package io.simplelogin.android.ui.home.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.api.MailboxLite
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.ui.home.cell.AliasCell
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.OptionRow
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsScreen() = with(hiltViewModel<DeviceSettingsViewModel>()) {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val settings by deviceSettings.collectAsState()

    Scaffold(
        modifier = Modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.device_settings)) },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .padding(horizontal = Spacing.regular)
                    .padding(innerPadding)
        ) {
            AliasCellSelectionSection(
                selected = settings.aliasCellSelection,
                onSelect = ::updateAliasCellSelection
            )

            AliasOptionsDisplaySection(
                selected = settings.aliasOptionsDisplay,
                onSelect = ::updateAliasOptionsDisplay
            )

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

@Composable
private fun AliasOptionsDisplaySection(
    selected: AliasOptionsDisplay,
    onSelect: (AliasOptionsDisplay) -> Unit
) {
    OptionRow(
        title = stringResource(R.string.alias_options_display),
        description = { it.title(LocalContext.current) },
        options = AliasOptionsDisplay.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun AliasCellSelectionSection(
    selected: AliasCellSelection,
    onSelect: (AliasCellSelection) -> Unit
) {
    OptionRow(
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
        title = stringResource(R.string.swipe_from_left_to_right),
        description = { it.title(LocalContext.current) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedLeftToRight,
        onSelect = onSelectLeftToRight
    )

    OptionRow(
        title = stringResource(R.string.swipe_from_right_to_left),
        description = { it.title(LocalContext.current) },
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedRightToLeft,
        onSelect = onSelectRightToLeft
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { showAliasDisplayInfosDialog = true }
    ) {
        Text(stringResource(R.string.alias_display))

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
            onDismiss = { showAliasDisplayInfosDialog = false },
            onSave = {
                showAliasDisplayInfosDialog = false
                onSaveAliasDisplayInfos(it)
            }
        )
    }
}

@OptIn(ExperimentalTime::class)
private val Alias.Companion.sample: Alias
    get() = Alias(
        id = AliasId(value = 0),
        email = "news.fejha@simplelogin.io",
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