package io.simplelogin.feature.auth.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.core.designsystem.NumericKeypad
import io.simplelogin.core.designsystem.NumericKeypadKey
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.feature.auth.R
import kotlinx.coroutines.launch

internal sealed class VerificationMode {
    data class Mfa(val key: String) : VerificationMode()
    data class Activation(val email: String, val onResend: () -> Unit) : VerificationMode()

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

@Suppress("CyclomaticComplexMethod", "ComplexCondition")
@Composable
internal fun VerificationDialog(
    mode: VerificationMode,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val scope = rememberCoroutineScope()
    val clipboard = LocalClipboard.current
    var resentCode by remember { mutableStateOf(false) }
    var manualEnter by remember { mutableStateOf(false) }
    var digit1 by remember { mutableStateOf<Int?>(null) }
    var digit2 by remember { mutableStateOf<Int?>(null) }
    var digit3 by remember { mutableStateOf<Int?>(null) }
    var digit4 by remember { mutableStateOf<Int?>(null) }
    var digit5 by remember { mutableStateOf<Int?>(null) }
    var digit6 by remember { mutableStateOf<Int?>(null) }
    val ableToConfirm by remember {
        derivedStateOf {
            digit1 != null && digit2 != null && digit3 != null && digit4 != null && digit5 != null && digit6 != null
        }
    }

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

                    NumericKeypad(
                        modifier = Modifier.fillMaxWidth(),
                        numbersEnabled = !ableToConfirm,
                        confirmEnabled = ableToConfirm,
                        onTap = { key ->
                            when (key) {
                                is NumericKeypadKey.Number -> {
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
                                    }
                                }

                                is NumericKeypadKey.Delete -> {
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

                                is NumericKeypadKey.Ok -> {
                                    if (digit1 != null &&
                                        digit2 != null &&
                                        digit3 != null &&
                                        digit4 != null &&
                                        digit5 != null &&
                                        digit6 != null
                                    ) {
                                        val code = "$digit1$digit2$digit3$digit4$digit5$digit6"
                                        onConfirm(code)
                                    }
                                }
                            }
                        }
                    )
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
                        onClick = {
                            scope.launch {
                                clipboard.getClipEntry()?.clipData?.let { clipData ->
                                    if (clipData.itemCount > 0) {
                                        clipData.getItemAt(0).text.toString().let {
                                            onConfirm(it)
                                        }
                                    }
                                }
                            }
                        }
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
                                        mode.onResend()
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
