package io.simplelogin.core.designsystem

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun SettingsFooter(
    modifier: Modifier = Modifier,
    padded: Boolean = true,
    text: String
) {
    Text(
        modifier = modifier
            .padding(horizontal = if (padded) Spacing.regular else 0.dp)
            .padding(top = if (padded) Spacing.small else 0.dp),
        text = text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.secondary
    )
}