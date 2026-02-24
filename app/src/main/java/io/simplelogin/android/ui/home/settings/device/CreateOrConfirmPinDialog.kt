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

enum class CreateOrEditPinMode {
    CREATE, CONFIRM
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateOrConfirmPinDialog(
    mode: CreateOrEditPinMode,
    minLength: Int = 4,
    maxLength: Int = 10,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var pin by rememberSaveable { mutableStateOf("") }
    var isRepeating by rememberSaveable { mutableStateOf(false) }
    var repeatedPin by rememberSaveable { mutableStateOf("") }
    var notMatched by rememberSaveable { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            val text = when (mode) {
                CreateOrEditPinMode.CREATE ->
                    if (isRepeating) stringResource(R.string.repeat_pin_code) else stringResource(R.string.choose_a_pin_code)

                CreateOrEditPinMode.CONFIRM -> stringResource(R.string.enter_current_pin_code)
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

                if (mode == CreateOrEditPinMode.CREATE) {
                    if (notMatched) {
                        Text(
                            text = stringResource(R.string.pins_not_matched),
                            color = Color.Red,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        Text(
                            text = if (isRepeating) "" else stringResource(R.string.pin_length_description),
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                NumericKeypad(
                    modifier = Modifier.fillMaxWidth(),
                    numbersEnabled = if (isRepeating) repeatedPin.length < maxLength else pin.length < maxLength,
                    confirmEnabled = if (isRepeating) repeatedPin.length >= minLength else pin.length >= minLength,
                    onTap = {
                        when (it) {
                            is NumericKeypadKey.Number -> {
                                if (isRepeating) {
                                    notMatched = false
                                    repeatedPin += it.value
                                } else {
                                    pin += it.value
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
                                    CreateOrEditPinMode.CREATE -> {
                                        if (isRepeating) {
                                            if (repeatedPin == pin) {
                                                onConfirm(pin)
                                            } else {
                                                repeatedPin = ""
                                                notMatched = true
                                            }
                                        } else {
                                            isRepeating = true
                                        }
                                    }

                                    CreateOrEditPinMode.CONFIRM -> onConfirm(pin)
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
