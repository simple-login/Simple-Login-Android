package io.simplelogin.android.ui.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.text.input.KeyboardType

// TODO: Localize strings here
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBaseUrlDialog(
    baseUrl: String,
    onDismiss: () -> Unit,
    onReset: () -> Unit,
    onSave: (String) -> Unit
) {
    var updatedBaseUrl by remember { mutableStateOf(baseUrl) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change API URL") },
        text = {
            Column {
                Text("Only change API URL if you're hosting SimpleLogin on your own")
                TextField(
                    value = updatedBaseUrl,
                    onValueChange = { updatedBaseUrl = it },
                    label = { Text("API URL") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                )
            }
        },
        confirmButton = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                TextButton(
                    onClick = { onSave(updatedBaseUrl) }
                ) {
                    Text("Save")
                }

                TextButton(onClick = onReset) {
                    Text("Reset to default value")
                }

                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        }
    )
}