package io.simplelogin.android.ui.home.lockscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.simplelogin.android.R
import io.simplelogin.core.designsystem.rememberBiometricAuthenticator
import io.simplelogin.android.ui.home.settings.device.CreateOrConfirmPinDialog
import io.simplelogin.android.ui.home.settings.device.CreateOrEditPinMode
import kotlinx.coroutines.launch
import io.simplelogin.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen(onLogOut: () -> Unit) = with(hiltViewModel<LockViewModel>()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val state by stateFlow.collectAsState()
    var showPinDialog by rememberSaveable { mutableStateOf(false) }
    var showLastAttemptDialog by rememberSaveable { mutableStateOf(false) }

    fun handleFailure() {
        scope.launch {
            when (recordFailure()) {
                LockScreenFailure.INVALID_ATTEMPT -> {}
                LockScreenFailure.LAST_ATTEMPT -> showLastAttemptDialog = true
                LockScreenFailure.SHOULD_LOG_OUT -> {
                    showPinDialog = false
                    onLogOut()
                }
            }
        }
    }

    val biometricallyAuthenticate = rememberBiometricAuthenticator(
        title = stringResource(R.string.please_authenticate),
        onSuccess = {
            scope.launch { unlock() }
        },
        onError = {
            scope.launch { recordFailure() }
        },
        onCancel = { handleFailure() },
        onNotAvailable = onLogOut
    )

    fun authenticate() {
        if (state.isPinProtected) {
            showPinDialog = true
        } else if (state.isBiometricProtected) {
            biometricallyAuthenticate()
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_STOP ->
                    scope.launch {
                        coverAndRecordLastBackgroundTime()
                    }

                Lifecycle.Event.ON_START ->
                    scope.launch {
                        updateLockState()
                    }

                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(state) {
        authenticate()
    }

    when (state) {
        is LockScreenState.Loading, is LockScreenState.Protected ->
            Dialog(
                onDismissRequest = {},
                properties = DialogProperties(
                    dismissOnBackPress = false,
                    dismissOnClickOutside = false,
                    usePlatformDefaultWidth = false
                )
            ) {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = MaterialTheme.colorScheme.background,
                    topBar = {
                        if (state is LockScreenState.Protected) {
                            TopAppBar(
                                title = {},
                                actions = {
                                    IconButton(onClick = onLogOut) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Outlined.Logout,
                                            contentDescription = null
                                        )
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    if (state is LockScreenState.Protected) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                painter = painterResource(id = DesignSystemR.drawable.ic_logo_with_name),
                                contentDescription = null,
                                modifier = Modifier.size(120.dp),
                                tint = Color.Gray
                            )

                            TextButton(onClick = { authenticate() }) {
                                Text(text = stringResource(R.string.unlock))
                            }
                        }
                    }
                }
            }

        else -> {}
    }

    if (showPinDialog) {
        val pinCode = (state as? LockScreenState.Protected)?.pinCode
        CreateOrConfirmPinDialog(
            mode = CreateOrEditPinMode.Confirm(pinCode),
            onConfirmSuccess = {
                showPinDialog = false
                scope.launch {
                    unlock()
                }
            },
            onConfirmFailure = { handleFailure() },
            onDismiss = { showPinDialog = false }
        )
    }

    if (showLastAttemptDialog) {
        AlertDialog(
            title = { Text(text = stringResource(R.string.last_attempt_title)) },
            text = { Text(text = stringResource(R.string.last_attempt_description)) },
            onDismissRequest = { showLastAttemptDialog = false },
            confirmButton = {
                TextButton(onClick = { showLastAttemptDialog = false }) {
                    Text(text = stringResource(R.string.close))
                }
            },
        )
    }
}