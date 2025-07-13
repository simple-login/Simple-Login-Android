package io.simplelogin.android.ui.home

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceSettingsDialog(
    onDismiss: () -> Unit
) {
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