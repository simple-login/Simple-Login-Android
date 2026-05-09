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
import io.simplelogin.core.common.isValidPassword
import io.simplelogin.feature.auth.R
import io.simplelogin.feature.auth.ui.EmailTextField
import io.simplelogin.feature.auth.ui.PasswordTextField

@Composable
internal fun SignUpDialog(
    onDismiss: () -> Unit,
    onSignUp: (email: String, password: String) -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.sign_up_for_sl)) },
        text = {
            LaunchedEffect(Unit) {
                focusRequester.requestFocus()
            }
            Column {
                EmailTextField(
                    modifier = Modifier.focusRequester(focusRequester),
                    value = email,
                    onValueChange = { email = it }
                )

                PasswordTextField(
                    value = password,
                    onValueChange = { password = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = email.isValidEmail() && password.isValidPassword(),
                onClick = { onSignUp(email, password) }
            ) {
                Text(stringResource(R.string.create_account))
            }
        }
    )
}
