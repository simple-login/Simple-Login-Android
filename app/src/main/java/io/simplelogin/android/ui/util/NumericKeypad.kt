package io.simplelogin.android.ui.util

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.min
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing

sealed class NumericKeypadKey {
    data class Number(val value: Int) : NumericKeypadKey()
    object Delete : NumericKeypadKey()
    object Ok : NumericKeypadKey()
}

@get:Composable
private val KeyTextStyle: androidx.compose.ui.text.TextStyle
    get() = MaterialTheme.typography.headlineLarge

private const val COLUMNS = 3

@Composable
fun NumericKeypad(
    numbersEnabled: Boolean,
    confirmEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTap: (NumericKeypadKey) -> Unit
) {
    val rows = listOf(
        listOf(NumericKeypadKey.Number(1), NumericKeypadKey.Number(2), NumericKeypadKey.Number(3)),
        listOf(NumericKeypadKey.Number(4), NumericKeypadKey.Number(5), NumericKeypadKey.Number(6)),
        listOf(NumericKeypadKey.Number(7), NumericKeypadKey.Number(8), NumericKeypadKey.Number(9)),
        listOf(NumericKeypadKey.Delete, NumericKeypadKey.Number(0), NumericKeypadKey.Ok)
    )

    // Measure the size needed for a number button content
    val textMeasurer = rememberTextMeasurer()
    val density = LocalDensity.current
    val textStyle = KeyTextStyle.copy(
        fontWeight = FontWeight.Bold,
        fontFamily = FontFamily.Monospace
    )
    val textSize = textMeasurer.measure("0", textStyle).size
    val minButtonSize: Dp = with(density) {
        maxOf(
            textSize.width,
            textSize.height
        ).toDp() + Spacing.regular * 2 + ButtonDefaults.TextButtonContentPadding.calculateTopPadding() * 2
    }

    BoxWithConstraints(modifier = modifier) {
        // Calculate button size based on available width
        // Available width = 3 buttons + 2 spacing gaps
        val maxButtonSizeFromWidth = (maxWidth - Spacing.regular * (COLUMNS - 1)) / COLUMNS
        val buttonSize = min(minButtonSize, maxButtonSizeFromWidth)

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(Spacing.regular)
            ) {
                rows.forEach { rowKeys ->
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.regular)) {
                        rowKeys.forEach { key ->
                            KeypadButton(
                                key = key,
                                size = buttonSize,
                                numbersEnabled = numbersEnabled,
                                confirmEnabled = confirmEnabled,
                                onTap = onTap
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun KeypadButton(
    key: NumericKeypadKey,
    size: Dp,
    numbersEnabled: Boolean,
    confirmEnabled: Boolean,
    modifier: Modifier = Modifier,
    onTap: (NumericKeypadKey) -> Unit
) {
    when (key) {
        is NumericKeypadKey.Number -> {
            TextButton(
                modifier = modifier.size(size),
                shape = CircleShape,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceBright,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = numbersEnabled,
                onClick = { onTap(key) }
            ) {
                Text(
                    text = "${key.value}",
                    style = KeyTextStyle,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        is NumericKeypadKey.Delete -> {
            TextButton(
                modifier = modifier.size(size),
                shape = CircleShape,
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

        is NumericKeypadKey.Ok -> {
            TextButton(
                modifier = modifier.size(size),
                shape = CircleShape,
                colors = ButtonDefaults.textButtonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = confirmEnabled,
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
