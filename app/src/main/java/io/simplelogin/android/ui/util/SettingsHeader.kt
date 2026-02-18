package io.simplelogin.android.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun SettingsHeader(
    modifier: Modifier = Modifier,
    text: String
) {
    Text(
        modifier = modifier
            .padding(horizontal = Spacing.regular)
            .padding(bottom = Spacing.small),
        text = text.uppercase(),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary
    )
}