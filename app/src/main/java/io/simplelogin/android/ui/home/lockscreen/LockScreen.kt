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
import io.simplelogin.android.data.models.preferences.DeviceLockType
import io.simplelogin.android.ui.home.settings.device.CreateOrConfirmPinDialog
import io.simplelogin.android.ui.home.settings.device.CreateOrEditPinMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LockScreen() = with(hiltViewModel<LockViewModel>()) {
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val state by stateFlow.collectAsState()
    var showPinDialog by rememberSaveable { mutableStateOf(false) }
    var showLastAttemptDialog by rememberSaveable { mutableStateOf(false) }

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
        if ((state as? LockScreenState.Protected)?.lockType == DeviceLockType.PIN) {
            showPinDialog = true
        }
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
                        TopAppBar(
                            title = {},
                            actions = {
                                IconButton(onClick = {
                                    scope.launch { logOut() }
                                }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Outlined.Logout,
                                        contentDescription = null
                                    )
                                }
                            }
                        )
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo_with_name),
                            contentDescription = null,
                            modifier = Modifier.size(120.dp),
                            tint = Color.Gray
                        )

                        TextButton(onClick = { showPinDialog = true }) {
                            Text(text = stringResource(R.string.unlock))
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
            onConfirmFailure = {
                scope.launch {
                    when (recordFailure()) {
                        LockScreenFailure.INVALID_ATTEMPT -> {}
                        LockScreenFailure.LAST_ATTEMPT -> showLastAttemptDialog = true
                        LockScreenFailure.SHOULD_LOG_OUT -> {
                            showPinDialog = false
                            logOut()
                        }
                    }
                }
            },
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