package io.simplelogin.android.ui.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.data.util.Constants

@Composable
fun LoginMasterScreen(modifier: Modifier) = with(hiltViewModel<LoginMasterScreenViewModel>()) {
    val baseUrl by baseUrlState.collectAsState()

    var showEditBaseUrlDialog by rememberSaveable { mutableStateOf(false) }
    var showSignInWithApiKeyDialog by rememberSaveable { mutableStateOf(false) }
    var showForgotPasswordDialog by rememberSaveable { mutableStateOf(false) }
    var showSignUpDialog by rememberSaveable { mutableStateOf(false) }

    LoginContentScreen(
        modifier = modifier,
        appVersion = appVersion,
        onLoginClick = { email, password -> login(email = email, password = password) },
        onLoginWithProtonClick = {},
        onSettingsClick = { showEditBaseUrlDialog = true },
        onSignInWithApiKeyClick = { showSignInWithApiKeyDialog = true },
        onForgotPasswordClick = { showForgotPasswordDialog = true },
        onSignUpClick = { showSignUpDialog = true }
    )

    if (showEditBaseUrlDialog) {
        EditBaseUrlDialog(
            baseUrl = baseUrl,
            onDismiss = { showEditBaseUrlDialog = false },
            onSave = {
                updateBaseUrl(baseUrl = it)
                showEditBaseUrlDialog = false
            },
            onUseDefault = {
                if (baseUrl != Constants.DEFAULT_BASE_URL) {
                    updateBaseUrl(baseUrl = Constants.DEFAULT_BASE_URL)
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
                login(apiKey = it)
            }
        )
    }

    if (showForgotPasswordDialog) {
        ForgotPasswordDialog(
            onDismiss = { showForgotPasswordDialog = false },
            onReset = {
                showForgotPasswordDialog = false
                forgotPassword(email = it)
            }
        )
    }

    if (showSignUpDialog) {
        SignUpDialog(
            onDismiss = { showSignUpDialog = false },
            onSignUp = { email, password ->
                showSignUpDialog = false
                signUp(email = email, password = password)
            }
        )
    }
}