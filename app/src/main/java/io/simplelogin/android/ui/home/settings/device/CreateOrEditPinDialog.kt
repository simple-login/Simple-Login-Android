package io.simplelogin.android.ui.home.settings.device

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.NumericKeypad
import io.simplelogin.android.ui.util.NumericKeypadKey

enum class CreateOrEditPinMode {
    CREATE, EDIT
}

private val MinPinLength = 4
private val MaxPinLength = 10

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrEditPinDialog(
    mode: CreateOrEditPinMode,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var isConfirming by rememberSaveable { mutableStateOf(false) }
    var confirmedPin by rememberSaveable { mutableStateOf("") }
    var notMatched by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val text = when (mode) {
                CreateOrEditPinMode.CREATE ->
                    if (isConfirming) stringResource(R.string.repeat_pin_code) else stringResource(R.string.choose_a_pin_code)

                CreateOrEditPinMode.EDIT -> ""
            }
            Text(text = text)
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(Spacing.regular))

                Text(
                    text = if (isConfirming) confirmedPin.map { "*" }.joinToString("")
                    else pin.map { "*" }.joinToString(""),
                    style = MaterialTheme.typography.headlineLarge
                )

                Text(
                    text = if (notMatched) stringResource(R.string.pins_not_matched) else "",
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(Spacing.regular))

                NumericKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    numbersEnabled = if (isConfirming) confirmedPin.length < MaxPinLength else pin.length < MaxPinLength,
                    confirmEnabled = if (isConfirming) confirmedPin.length >= MinPinLength else pin.length >= MinPinLength,
                    onTap = {
                        when (it) {
                            is NumericKeypadKey.Number -> {
                                if (isConfirming) {
                                    notMatched = false
                                    confirmedPin += it.value
                                } else {
                                    pin += it.value
                                }
                            }

                            is NumericKeypadKey.Delete -> {
                                if (isConfirming) {
                                    notMatched = false
                                    confirmedPin = confirmedPin.dropLast(1)
                                } else {
                                    pin = pin.dropLast(1)
                                }
                            }

                            is NumericKeypadKey.Ok -> {
                                when (mode) {
                                    CreateOrEditPinMode.CREATE -> {
                                        if (isConfirming) {
                                            if (confirmedPin == pin) {
                                                onConfirm(pin)
                                            } else {
                                                notMatched = true
                                            }
                                        } else {
                                            isConfirming = true
                                        }
                                    }

                                    CreateOrEditPinMode.EDIT -> {}
                                }
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {}
    )
}
