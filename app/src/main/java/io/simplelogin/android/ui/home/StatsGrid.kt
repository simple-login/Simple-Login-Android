package io.simplelogin.android.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun StatsGrid(
    modifier: Modifier = Modifier,
    stats: Stats
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            StatsCell(
                title = stringResource(R.string.all_aliases),
                description = stringResource(R.string.all_time),
                value = stats.aliasCount
            )

            StatsCell(
                title = stringResource(R.string.forward),
                description = stringResource(R.string.last_14_days),
                value = stats.forwardCount
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
            StatsCell(
                title = stringResource(R.string.replies_send),
                description = stringResource(R.string.last_14_days),
                value = stats.replyCount
            )

            StatsCell(
                title = stringResource(R.string.blocked),
                description = stringResource(R.string.last_14_days),
                value = stats.blockCount
            )
        }
    }
}

@Composable
private fun StatsCell(
    modifier: Modifier = Modifier,
    title: String,
    description: String,
    value: Int
) {
    Column (
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(Spacing.medium)
                )
            .padding(Spacing.medium)
            .clip(RoundedCornerShape(Spacing.medium))
    ) {
        Row (
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
        ) {
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