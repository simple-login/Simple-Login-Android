package io.simplelogin.core.designsystem

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun TitledFAB(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.mediumLarge)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = SlColor.BackgroundColor,
            shadowElevation = 2.dp
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(
                    horizontal = Spacing.mediumLarge,
                    vertical = Spacing.medium
                )
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = title
            )
        }
    }
}
