package io.simplelogin.feature.auth.ui.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.stringResource
import io.simplelogin.core.common.isValidEmail
import io.simplelogin.feature.auth.R
import io.simplelogin.feature.auth.ui.EmailTextField

@Composable
internal fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onReset: (String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var emailAddress by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.forgot_password)) },
        text = {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            Column {
                Text(stringResource(R.string.forgot_password_instruction))
                EmailTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = emailAddress,
                    onValueChange = { emailAddress = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = emailAddress.isValidEmail(),
                onClick = { onReset(emailAddress) }
            ) {
                Text(stringResource(R.string.reset_password))
            }
        }
    )
}