package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.IconContent

@Composable
fun StatsGrid(
    modifier: Modifier = Modifier,
    stats: Stats
) {
    Row(
        modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            StatsCell(
                title = stringResource(R.string.all_aliases),
                description = stringResource(R.string.all_time),
                value = stats.aliasCount,
                icon = IconContent.ImageVectorContent(Icons.Default.AlternateEmail)
            )

            StatsCell(
                title = stringResource(R.string.forward),
                description = stringResource(R.string.last_14_days),
                value = stats.forwardCount,
                icon = IconContent.ImageVectorContent(Icons.AutoMirrored.Filled.Send)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            StatsCell(
                title = stringResource(R.string.replies_send),
                description = stringResource(R.string.last_14_days),
                value = stats.replyCount,
                icon = IconContent.ImageVectorContent(Icons.AutoMirrored.Filled.Reply)
            )

            StatsCell(
                title = stringResource(R.string.blocked),
                description = stringResource(R.string.last_14_days),
                value = stats.blockCount,
                icon = IconContent.ImageVectorContent(Icons.Default.Block)
            )
        }
    }
}

@Composable
private fun StatsCell(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    value: Int,
    icon: IconContent
) {
    Box(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.background,
                shape = RoundedCornerShape(Spacing.medium)
            )
            .padding(Spacing.medium)
            .clip(RoundedCornerShape(Spacing.medium))
    ) {
        val iconModifier = Modifier
            .scale(2f)
            .alpha(0.1f)
            .align(Alignment.CenterEnd)
            .padding(end = Spacing.medium)

        val iconTint = MaterialTheme.colorScheme.onSurfaceVariant

        when (icon) {
            is IconContent.ImageVectorContent -> Icon(
                modifier = iconModifier,
                imageVector = icon.vector,
                contentDescription = icon.contentDescription,
                tint = iconTint
            )

            is IconContent.PainterContent -> Icon(
                modifier = iconModifier,
                painter = icon.painter,
                contentDescription = icon.contentDescription,
                tint = iconTint
            )
        }

        Column {
            Row {
                Text(
                    text = title,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text(
                text = "$value",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
        }
    }
}