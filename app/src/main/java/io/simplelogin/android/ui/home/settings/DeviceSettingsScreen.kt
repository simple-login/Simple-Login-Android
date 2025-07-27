package io.simplelogin.android.ui.home.settings

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.MailboxLite
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.AliasDisplayMode
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
        Column(modifier =
            Modifier
                .padding(horizontal = Spacing.regular)
                .padding(innerPadding)
        ) {
            SecuritySection()

            AliasCellSelectionSection(
                selected = settings.aliasCellSelection,
                onSelect = ::updateAliasCellSelection
            )

            SwipeActionSelection(
                selectedLeftToRight = settings.swipeFromLeftToRightAction,
                onSelectLeftToRight = ::updateSwipeFromLeftToRight,
                selectedRightToLeft = settings.swipeFromRightToLeftAction,
                onSelectRightToLeft = ::updateSwipeFromRightToLeft,
                selectedAliasDisplayMode = settings.aliasDisplayMode,
                onSelectAliasDisplayMode = ::updateAliasDisplayMode
            )
        }
    }
}

@Composable
private fun SecuritySection() {
    Text(
        text = stringResource(R.string.security)
    )
}

@Composable
private fun AliasCellSelectionSection(
    selected: AliasCellSelection,
    onSelect: (AliasCellSelection) -> Unit
) {
    OptionRow(
        title = stringResource(R.string.select_alias_action),
        description = {
            when (it) {
                AliasCellSelection.VIEW_DETAILS -> stringResource(R.string.view_details)
                AliasCellSelection.COPY_EMAIL -> stringResource(R.string.copy_alias_address)
            }
        },
        options = AliasCellSelection.entries.toTypedArray(),
        selected = selected,
        onSelect = onSelect
    )
}

@Composable
private fun SwipeActionSelection(
    selectedLeftToRight: SwipeAction,
    onSelectLeftToRight: (SwipeAction) -> Unit,
    selectedRightToLeft: SwipeAction,
    onSelectRightToLeft: (SwipeAction) -> Unit,
    selectedAliasDisplayMode: AliasDisplayMode,
    onSelectAliasDisplayMode: (AliasDisplayMode) -> Unit,
) {
    val description: @Composable (SwipeAction) -> String = {
        when (it) {
            SwipeAction.DISABLE_ENABLE -> stringResource(R.string.disable_enable)
            SwipeAction.PIN_UNPIN -> stringResource(R.string.pin_unpin)
            SwipeAction.DELETE -> stringResource(R.string.delete)
        }
    }

    OptionRow(
        title = stringResource(R.string.swipe_from_left_to_right),
        description = description,
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedLeftToRight,
        onSelect = onSelectLeftToRight
    )

    OptionRow(
        title = stringResource(R.string.swipe_from_right_to_left),
        description = description,
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedRightToLeft,
        onSelect = onSelectRightToLeft
    )

    OptionRow(
        title = stringResource(R.string.alias_display_mode),
        description = {
            when (it) {
                AliasDisplayMode.DEFAULT -> stringResource(R.string.alias_display_mode_default)
                AliasDisplayMode.COMFORTABLE -> stringResource(R.string.alias_display_mode_comfortable)
                AliasDisplayMode.COMPACT -> stringResource(R.string.alias_display_mode_compact)
            }
        },
        options = AliasDisplayMode.entries.toTypedArray(),
        selected = selectedAliasDisplayMode,
        onSelect = onSelectAliasDisplayMode
    )

    AliasCell(
        alias = Alias.sample,
        displayMode = selectedAliasDisplayMode,
        swipeFromStartToEndAction = selectedLeftToRight,
        swipeFromEndToStartAction = selectedRightToLeft,
        onAction = null
    )
}

@OptIn(ExperimentalTime::class)
private val Alias.Companion.sample: Alias
    get() = Alias(
        id = 0,
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