package io.simplelogin.android.ui.login

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.ProtonPurple
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.clickableRippleDisabled

@Composable
fun LoginContentScreen(
    modifier: Modifier,
    appVersion: String,
    onLoginClick: (email: String, password: String) -> Unit,
    onLoginWithProtonClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onSignInWithApiKeyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    val focusManager = LocalFocusManager.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .clickableRippleDisabled { focusManager.clearFocus() },
        contentAlignment = Alignment.Center,
    ) {
        LoginColumn(
            modifier = Modifier.width(280.dp),
            onLoginClick = onLoginClick,
            onLoginWithProtonClick = onLoginWithProtonClick,
            onSignInWithApiKeyClick = onSignInWithApiKeyClick,
            onForgotPasswordClick = onForgotPasswordClick,
            onSignUpClick = onSignUpClick
        )

        Text(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = Spacing.regular),
            text = appVersion,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        IconButton(
            onClick = onSettingsClick,
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginColumn(
    modifier: Modifier,
    onLoginClick: (email: String, password: String) -> Unit,
    onLoginWithProtonClick: () -> Unit,
    onSignInWithApiKeyClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onSignUpClick: () -> Unit
) {
    var emailAddress by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var showMoreOptions by rememberSaveable { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        EmailTextField(
            modifier = Modifier.fillMaxWidth(),
            value = emailAddress,
            onValueChange = { emailAddress = it }
        )

        PasswordTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.large),
            value = password,
            onValueChange = { password = it }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { onLoginClick(emailAddress, password) }
        ) {
            Text(stringResource(R.string.sign_in))
        }

        Text(
            text = stringResource(R.string.or),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        CompositionLocalProvider(
            LocalRippleConfiguration provides RippleConfiguration(
                color = ProtonPurple,
                rippleAlpha = null
            )
        ) {
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