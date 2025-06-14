package io.simplelogin.android.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import io.simplelogin.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBaseUrlDialog(
    baseUrl: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onUseDefault: () -> Unit
) {
    var updatedBaseUrl by remember { mutableStateOf(baseUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.api_url)) },
        text = {
            Column {
                Text(stringResource(R.string.change_api_url_explanation))
                TextField(
                    value = updatedBaseUrl,
                    onValueChange = { updatedBaseUrl = it },
                    placeholder = { Text(stringResource(R.string.api_url)) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                TextButton(
                    onClick = { onSave(updatedBaseUrl) }
                ) {
                    Text(stringResource(R.string.save))
                }

                TextButton(onClick = onUseDefault) {
                    Text(stringResource(R.string.use_default_api_url))
                }
            }
        }
    )
}