package io.simplelogin.android.ui.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.OptionRow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsDialog(
    onDismiss: () -> Unit
) = with(hiltViewModel<DeviceSettingsDialogViewModel>()) {
    val settings by deviceSettings.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = Spacing.medium)
        ) {
            Column {
                TopAppBar(
                    title = { Text(stringResource(R.string.device_settings)) },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                )

                SecuritySection()

                AliasCellSelectionSection(
                    selected = settings.aliasCellSelection,
                    onSelect = ::updateAliasCellSelection
                )

                SwipeActionSelection(
                    selectedLeftToRight = settings.swipeFromLeftToRightAction,
                    onSelectLeftToRight = ::updateSwipeFromLeftToRight,
                    selectedRightToLeft = settings.swipeFromRightToLeftAction,
                    onSelectRightToLeft = ::updateSwipeFromRightToLeft
                )
            }
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
    onSelectRightToLeft: (SwipeAction) -> Unit
) {
    OptionRow(
        title = stringResource(R.string.swipe_from_left_to_right),
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedLeftToRight,
        onSelect = onSelectLeftToRight
    )

    OptionRow(
        title = stringResource(R.string.swipe_from_right_to_left),
        options = SwipeAction.entries.toTypedArray(),
        selected = selectedRightToLeft,
        onSelect = onSelectRightToLeft
    )
}