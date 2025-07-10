package io.simplelogin.android.ui.login

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import io.simplelogin.android.R
import io.simplelogin.android.util.isValidEmail
import io.simplelogin.android.util.isValidPassword

@Composable
fun EmailTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        modifier = modifier,
        value = value,
        label = { Text(stringResource(R.string.email_address)) },
        placeholder = { Text(stringResource(R.string.email_address)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        trailingIcon = {
            IconButton(onClick = { onValueChange("") }) {
                Icon(
                    imageVector = Icons.Outlined.Cancel,
                    contentDescription = stringResource(R.string.clear_email_address)
                )
            }
        },
        onValueChange = onValueChange,
        isError = !value.isEmpty() && !value.isValidEmail(),
        supportingText = {
            if (!value.isEmpty() && !value.isValidEmail()) {
                Text(stringResource(R.string.invalid_email_error))
            }
        }
    )
}

@Composable
fun PasswordTextField(
    modifier: Modifier = Modifier,
    value: String,
    onValueChange: (String) -> Unit
) {
    var showPassword by rememberSaveable { mutableStateOf(false) }
    OutlinedTextField(
        modifier = modifier
            .onFocusChanged {
                // Automatically hide password when password text field loses focus
                if (showPassword && !it.isFocused) {
                    showPassword = false
                }
            },
        value = value,
        label = { Text(stringResource(R.string.password)) },
        placeholder = { Text(stringResource(R.string.password)) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            IconButton(onClick = { showPassword = !showPassword }) {
                Icon(
                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = stringResource(R.string.show_or_hide_password)
                )
            }
        },
        onValueChange = onValueChange,
        isError = !value.isEmpty() && !value.isValidPassword(),
        supportingText = {
            if (!value.isEmpty() && !value.isValidPassword()) {
                Text(stringResource(R.string.password_length_error))
            }
        }
    )
}