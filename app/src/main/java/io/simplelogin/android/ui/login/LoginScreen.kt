package io.simplelogin.android.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.outlined.Cancel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.data.util.Constants
import io.simplelogin.android.ui.theme.ProtonPurple
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun LoginScreen(
    modifier: Modifier,
    appVersion: String,
    baseUrl: String,
    onBaseUrlChange: (String) -> Unit,
    onLoginClick: () -> Unit,
    onLoginWithProtonClick: () -> Unit,
    onLoginWithApiKeyClick: (String) -> Unit,
    onForgotPassword: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current
    var showEditBaseUrlDialog by remember { mutableStateOf(false) }
    var showSignInWithApiKeyDialog by remember { mutableStateOf(false) }
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var showSignUpDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickable(
                // Disable ripple effect
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { focusManager.clearFocus() }
            ),
        contentAlignment = Alignment.Center,
    ) {
        LoginColumn(
            modifier = Modifier.width(280.dp),
            onLoginClick = onLoginClick,
            onLoginWithProtonClick = onLoginWithProtonClick,
            onSignInWithApiKeyClick = { showSignInWithApiKeyDialog = true },
            onForgotPasswordClick = { showForgotPasswordDialog = true },
            onSignUpClick = { showSignUpDialog = true }
        )

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

    if (showSignInWithApiKeyDialog) {
        SetApiKeyDialog(
            onDismiss = { showSignInWithApiKeyDialog = false },
            onSet = {
                showSignInWithApiKeyDialog = false
                onLoginWithApiKeyClick(it)
            }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onReset = {
                showForgotPasswordDialog = false
                onForgotPassword(it)
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginColumn(
    modifier: Modifier,
    onLoginClick: () -> Unit,
    onLoginWithProtonClick: () -> Unit,
    onSignInWithApiKeyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var emailAddress by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showMoreOptions by remember { mutableStateOf(false) }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = emailAddress,
            label = { Text(stringResource(R.string.email_address)) },
            placeholder = { Text(stringResource(R.string.email_address)) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            trailingIcon = {
                IconButton(onClick = { emailAddress = "" }) {
                    Icon(
                        imageVector = Icons.Outlined.Cancel,
                        contentDescription = stringResource(R.string.clear_email_address)
                    )
                }
            },
            onValueChange = { emailAddress = it },
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.large)
                .onFocusChanged {
                    // Automatically hide password when password text field loses focus
                    if (showPassword && !it.isFocused) {
                        showPassword = false
                    }
                },
            value = password,
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
            onValueChange = { password = it },
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onLoginClick
        ) {
            Text(stringResource(R.string.sign_in))
        }

        Text(
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CompositionLocalProvider(LocalRippleConfiguration provides RippleConfiguration(
            color = ProtonPurple,
            rippleAlpha = null
        )) {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth(),
                border = BorderStroke(1.dp, ProtonPurple),
                onClick = onLoginWithProtonClick
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_proton),
                            tint = Color.Unspecified,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.fillMaxWidth())
                    }

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(R.string.log_in_with_proton),
                        textAlign = TextAlign.Center,
                        color = ProtonPurple
                    )
                }
            }
        }

        AnimatedVisibility(!showMoreOptions) {
            TextButton(
                colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                onClick = { showMoreOptions = true }
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.more),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }

        AnimatedVisibility(showMoreOptions) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = Spacing.regular),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    onClick = onSignInWithApiKeyClick
                ) {
                    Text(stringResource(R.string.sign_in_with_api_key))
                }

                TextButton(onClick = onForgotPasswordClick) {
                    Text(stringResource(R.string.forgot_password))
                }

                HorizontalDivider()

                TextButton(onClick = onSignUpClick) {
                    Text(stringResource(R.string.sign_up))
                }
            }
        }
    }
}