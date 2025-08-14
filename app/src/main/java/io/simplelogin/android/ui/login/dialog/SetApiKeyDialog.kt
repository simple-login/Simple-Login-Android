package io.simplelogin.android.ui.login.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiKey
import kotlinx.coroutines.launch

@Composable
fun SetApiKeyDialog(
    onDismiss: () -> Unit,
    onSet: (ApiKey) -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    var showTextField by rememberSaveable { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    var apiKey by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.enter_api_key)) },
        text = {
            Column {
                Text(stringResource(R.string.get_api_key_instructions))
                AnimatedVisibility(showTextField) {
                    LaunchedEffect(showTextField) {
                        focusRequester.requestFocus()
                    }
                    TextField(
                        modifier = Modifier.focusRequester(focusRequester),
                        value = apiKey,
                        placeholder = { Text(stringResource(R.string.api_key)) },
                        onValueChange = { apiKey = it }
                    )
                }
            }
        },
        confirmButton = {
            AnimatedVisibility(showTextField) {
                TextButton(
                    enabled = apiKey.isNotEmpty(),
                    onClick = { onSet(ApiKey(value = apiKey)) }
                ) {
                    Text(stringResource(R.string.confirm))
                }
            }

            AnimatedVisibility(!showTextField) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = {
                            scope.launch {
                                clipboard.getClipEntry()?.clipData?.let { clipData ->
                                    if (clipData.itemCount > 0) {
                                        val item = clipData.getItemAt(0)
                                        item.text?.let { text ->
                                            onSet(ApiKey(value = text.toString()))
                                        }
                                    }
                                }
                            }
                        }
                    ) {
                        Text(stringResource(R.string.paste_from_clipboard))
                    }

                    TextButton(
                        onClick = { showTextField = true }
                    ) {
                        Text(stringResource(R.string.enter_manually))
                    }
                }
            }
        }
    )
}