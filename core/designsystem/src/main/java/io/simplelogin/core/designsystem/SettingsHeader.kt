package io.simplelogin.core.designsystem

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun SettingsHeader(
    modifier: Modifier = Modifier,
    padded: Boolean = true,
    text: String
) {
    Surface(color = SlColor.BackgroundColor) {
        Text(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = if (padded) Spacing.regular else 0.dp)
                .padding(bottom = if (padded) Spacing.small else 0.dp),
            text = text.uppercase(),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.secondary
        )
    }
}