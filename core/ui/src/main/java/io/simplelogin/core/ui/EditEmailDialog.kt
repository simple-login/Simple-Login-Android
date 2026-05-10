package io.simplelogin.core.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import io.simplelogin.core.common.isValidEmail
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun EditEmailDialog(
    title: String,
    description: String? = null,
    ctaTitle: String,
    onAdd: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var email by remember { mutableStateOf("") }
    var isValidEmail by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            Column {
                description?.let {
                    Text(text = it)
                    Spacer(modifier = Modifier.height(Spacing.medium))
                }

                OutlinedTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = email,
                    onValueChange = {
                        isValidEmail = true
                        email = it
                    },
                    label = { Text(text = stringResource(R.string.email_address)) },
                    singleLine = true,
                    isError = !isValidEmail,
                    supportingText = {
                        if (!isValidEmail) {
                            Text(text = stringResource(R.string.invalid_email_error))
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Done
                    )
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                isValidEmail = email.isValidEmail()
                if (isValidEmail) {
                    onAdd(email)
                }
            }) {
                Text(text = ctaTitle)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}
