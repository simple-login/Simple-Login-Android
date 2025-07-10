package io.simplelogin.android.ui.login.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing

sealed class VerificationMode {
    object Mfa: VerificationMode()
    data class Activation(val email: String): VerificationMode()

    val titleRes: Int
        get() = when (this) {
            is Mfa -> R.string.enter_otp
            is Activation -> R.string.enter_activation_code
        }

    val descriptionRes: Int
        get() = when (this) {
            is Mfa -> R.string.enter_otp_description
            is Activation -> R.string.enter_activation_code_description
        }
}

@Composable
fun VerificationDialog(
    mode: VerificationMode,
    onDismiss: () -> Unit,
    onConfirm: (code: String) -> Unit,
    onResend: (email: String) -> Unit
) {
    var resentCode by remember { mutableStateOf(false) }
    var manualEnter by rememberSaveable { mutableStateOf(false) }
    var digit1 by rememberSaveable { mutableStateOf<Int?>(null) }
    var digit2 by rememberSaveable { mutableStateOf<Int?>(null) }
    var digit3 by rememberSaveable { mutableStateOf<Int?>(null) }
    var digit4 by rememberSaveable { mutableStateOf<Int?>(null) }
    var digit5 by rememberSaveable { mutableStateOf<Int?>(null) }
    var digit6 by rememberSaveable { mutableStateOf<Int?>(null) }
    var ableToConfirm by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(mode.titleRes)) },
        text = {
            if (manualEnter) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(Spacing.extraLarge)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentWidth(Alignment.CenterHorizontally),
                        horizontalArrangement = Arrangement.spacedBy(Spacing.large)
                    ) {
                        DigitText(digit1)
                        DigitText(digit2)
                        DigitText(digit3)
                        DigitText(digit4)
                        DigitText(digit5)
                        DigitText(digit6)
                    }

                    VerificationKeyboard(
                        ableToConfirm = ableToConfirm,
                        onTap = { key ->
                        when (key) {
                            is VerificationKeyboardKey.Number -> {
                                val value = key.value
                                if (digit1 == null) {
                                    digit1 = value
                                } else if (digit2 == null) {
                                    digit2 = value
                                } else if (digit3 == null) {
                                    digit3 = value
                                } else if (digit4 == null) {
                                    digit4 = value
                                } else if (digit5 == null) {
                                    digit5 = value
                                } else if (digit6 == null) {
                                    digit6 = value
                                    ableToConfirm = true
                                }
                            }
                            is VerificationKeyboardKey.Delete -> {
                                ableToConfirm = false
                                if (digit6 != null) {
                                    digit6 = null
                                } else if (digit5 != null) {
                                    digit5 = null
                                } else if (digit4 != null) {
                                    digit4 = null
                                } else if (digit3 != null) {
                                    digit3 = null
                                } else if (digit2 != null) {
                                    digit2 = null
                                } else if (digit1 != null) {
                                    digit1 = null
                                }
                            }
                            is VerificationKeyboardKey.Ok -> {
                                if (digit1 != null &&
                                    digit2 != null &&
                                    digit3 != null &&
                                    digit4 != null &&
                                    digit5 != null &&
                                    digit6 != null) {
                                    val code = "$digit1$digit2$digit3$digit4$digit5$digit6"
                                    onConfirm(code)
                                }
                            }
                        }
                    })
                }
            } else {
                Text(stringResource(mode.descriptionRes))
            }
        },
        confirmButton = {
            AnimatedVisibility(!manualEnter) {
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    TextButton(
                        onClick = {}
                    ) {
                        Text(stringResource(R.string.paste_from_clipboard))
                    }

                    TextButton(
                        onClick = { manualEnter = true }
                    ) {
                        Text(stringResource(R.string.enter_manually))
                    }


                    when (mode) {
                        is VerificationMode.Activation -> {
                            AnimatedVisibility(!resentCode) {
                                TextButton(
                                    onClick = {
                                        onResend(mode.email)
                                        resentCode = true
                                    }
                                ) {
                                    Text(stringResource(R.string.resend_activation_code))
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    )
}

@Composable
private fun DigitText(value: Int?) {
    Text(
        text = if (value != null) "$value" else "_",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
}

private sealed class VerificationKeyboardKey {
    data class Number(val value: Int): VerificationKeyboardKey()
    object Delete: VerificationKeyboardKey()
    object Ok: VerificationKeyboardKey()
}

@Composable
private fun VerificationKeyboard(
    ableToConfirm: Boolean,
    onTap: (VerificationKeyboardKey) -> Unit
) {
    val keys = listOf<VerificationKeyboardKey>(
        VerificationKeyboardKey.Number(1),
        VerificationKeyboardKey.Number(2),
        VerificationKeyboardKey.Number(3),
        VerificationKeyboardKey.Number(4),
        VerificationKeyboardKey.Number(5),
        VerificationKeyboardKey.Number(6),
        VerificationKeyboardKey.Number(7),
        VerificationKeyboardKey.Number(8),
        VerificationKeyboardKey.Number(9),
        VerificationKeyboardKey.Delete,
        VerificationKeyboardKey.Number(0),
        VerificationKeyboardKey.Ok
    )
    LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.regular),
        horizontalArrangement = Arrangement.spacedBy(Spacing.regular)
    ) {
        items(keys) { key ->
            when (key) {
                is VerificationKeyboardKey.Number -> {
                    TextButton(
                        enabled = !ableToConfirm,
                        modifier = Modifier
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceBright,
                                shape = CircleShape
                            )
                        ,
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { onTap(key) }
                    ) {
                        DigitText(key.value)
                    }
                }
                is VerificationKeyboardKey.Delete -> {
                    TextButton(
                        modifier = Modifier.aspectRatio(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { onTap(key) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
                is VerificationKeyboardKey.Ok -> {
                    TextButton(
                        enabled = ableToConfirm,
                        modifier = Modifier.aspectRatio(1f),
                        colors = ButtonDefaults.textButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = { onTap(key) }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = stringResource(R.string.delete)
                        )
                    }
                }
            }
        }
    }
}