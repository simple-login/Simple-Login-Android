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
import androidx.compose.ui.text.style.TextAlign
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.NumericKeypad
import io.simplelogin.android.ui.util.NumericKeypadKey

sealed class CreateOrEditPinMode {
    data object Create : CreateOrEditPinMode()
    data class Confirm(val pinCode: String?) : CreateOrEditPinMode()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrConfirmPinDialog(
    mode: CreateOrEditPinMode,
    minLength: Int = 4,
    maxLength: Int = 10,
    onCreate: ((String) -> Unit)? = null,
    onConfirmSuccess: (() -> Unit)? = null,
    onConfirmFailure: (() -> Unit)? = null,
    onDismiss: (() -> Unit)
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var isRepeating by rememberSaveable { mutableStateOf(false) }
    var repeatedPin by rememberSaveable { mutableStateOf("") }
    var notMatched by rememberSaveable { mutableStateOf(false) }
    var invalidPin by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val text = when (mode) {
                is CreateOrEditPinMode.Create ->
                    if (isRepeating) stringResource(R.string.repeat_pin_code) else stringResource(R.string.choose_a_pin_code)

                is CreateOrEditPinMode.Confirm -> stringResource(R.string.enter_current_pin_code)
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
                    text = if (isRepeating) repeatedPin.map { "*" }.joinToString("")
                    else pin.map { "*" }.joinToString(""),
                    style = MaterialTheme.typography.headlineLarge
                )

                val descriptionText = if (invalidPin) {
                    stringResource(R.string.invalid_pin_code)
                } else if (notMatched) {
                    stringResource(R.string.pins_not_matched)
                } else if (mode !is CreateOrEditPinMode.Confirm) {
                    stringResource(R.string.pin_length_description)
                } else {
                    ""
                }

                val descriptionTextColor = if (invalidPin || notMatched) {
                    Color.Red
                } else {
                    MaterialTheme.colorScheme.secondary
                }

                Text(
                    text = descriptionText,
                    color = descriptionTextColor,
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(Spacing.large))

                NumericKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    numbersEnabled = if (isRepeating) repeatedPin.length < maxLength else pin.length < maxLength,
                    confirmEnabled = if (isRepeating) repeatedPin.length >= minLength else pin.length >= minLength,
                    onTap = { key ->
                        when (key) {
                            is NumericKeypadKey.Number -> {
                                if (isRepeating) {
                                    notMatched = false
                                    repeatedPin += key.value
                                } else {
                                    pin += key.value
                                    invalidPin = false
                                }
                            }

                            is NumericKeypadKey.Delete -> {
                                if (isRepeating) {
                                    notMatched = false
                                    repeatedPin = repeatedPin.dropLast(1)
                                } else {
                                    pin = pin.dropLast(1)
                                }
                            }

                            is NumericKeypadKey.Ok -> {
                                when (mode) {
                                    is CreateOrEditPinMode.Create -> {
                                        if (isRepeating) {
                                            if (repeatedPin == pin) {
                                                onCreate?.let { it(pin) }
                                            } else {
                                                repeatedPin = ""
                                                notMatched = true
                                            }
                                        } else {
                                            isRepeating = true
                                        }
                                    }

                                    is CreateOrEditPinMode.Confirm -> {
                                        if (mode.pinCode == pin) {
                                            onConfirmSuccess?.let { it() }
                                        } else {
                                            pin = ""
                                            invalidPin = true
                                            onConfirmFailure?.let { it() }
                                        }
                                    }
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
