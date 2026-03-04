package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun ActivityStats(
    modifier: Modifier = Modifier,
    showLabel: Boolean,
    forward: Int,
    reply: Int,
    block: Int,
) {
    val total = forward + reply + block

    @Composable
    fun Divider() {
        VerticalDivider(modifier = Modifier.fillMaxHeight(0.6f))
    }
    Row(
        modifier = modifier.height(IntrinsicSize.Min),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatItem(
            modifier = Modifier.weight(1f),
            showLabel = true,
            title = stringResource(R.string.total),
            titleColor = MaterialTheme.colorScheme.secondary,
            value = total
        )

        Divider()

        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.Send,
            showLabel = showLabel,
            title = stringResource(R.string.forward),
            titleColor = SlColor.Green,
            value = forward
        )

        Divider()

        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.AutoMirrored.Filled.Reply,
            showLabel = showLabel,
            title = stringResource(R.string.reply),
            titleColor = SlColor.Blue,
            value = reply
        )

        Divider()

        StatItem(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Block,
            showLabel = showLabel,
            title = stringResource(R.string.block),
            titleColor = SlColor.Red,
            value = block
        )
    }
}

@Composable
private fun StatItem(
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    showLabel: Boolean,
    title: String,
    titleColor: Color,
    value: Int
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            icon?.let {
                Icon(
                    modifier = Modifier.size(LocalTextStyle.current.fontSize.value.dp),
                    imageVector = icon,
                    tint = titleColor,
                    contentDescription = null
                )
            }

            if (showLabel) {
                Text(
                    text = "${title}:",
                    color = titleColor,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Visible,
                    softWrap = false
                )
            }

            Text(
                text = "$value",
                maxLines = 1,
                overflow = TextOverflow.Visible,
                softWrap = false,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
