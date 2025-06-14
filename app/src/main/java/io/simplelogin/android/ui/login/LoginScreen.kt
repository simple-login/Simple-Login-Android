package io.simplelogin.android.ui.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun LoginScreen(
    modifier: Modifier,
    appVersion: String,
    baseUrl: String,
    onLoginClick: () -> Unit,
    onBaseUrlChange: (String) -> Unit
) {
    var showEditBaseUrlDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text(baseUrl)
            Button(onClick = onLoginClick) {
                Text("Login")
            }
        }

        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Spacing.regular),
            text = appVersion,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = { showEditBaseUrlDialog = true },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(end = Spacing.regular)
        ) {
            Icon(
                imageVector = Icons.Default.Settings,
                contentDescription = stringResource(R.string.settings)
            )
        }
    }

    if (showEditBaseUrlDialog) {
        EditBaseUrlDialog(
            baseUrl = baseUrl,
            onDismiss = { showEditBaseUrlDialog = false },
            onSave = {
                onBaseUrlChange(it)
                showEditBaseUrlDialog = false
            },
            onUseDefault = {
                if (baseUrl != Constants.DEFAULT_BASE_URL) {
                    onBaseUrlChange(Constants.DEFAULT_BASE_URL)
                }
                showEditBaseUrlDialog = false
            }
        )
    }
}