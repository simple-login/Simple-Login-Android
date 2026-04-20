package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ForwardToInbox
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import io.simplelogin.core.common.relativeTimeSpan
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.ActivityAction
import io.simplelogin.core.model.api.Alias

@Composable
fun AliasLatestActivityRow(activity: Alias.LatestActivity) = key(activity) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val fontSize = LocalTextStyle.current.fontSize
        val iconSize = with(LocalDensity.current) { fontSize.toDp() }
        Icon(
            modifier = Modifier.size(iconSize),
            imageVector = activity.action.icon,
            contentDescription = null,
            tint = activity.action.color
        )

        Text(
            modifier = Modifier.weight(1f, fill = false),
            text = activity.contact.email,
            maxLines = 1,
            overflow = TextOverflow.MiddleEllipsis
        )
        Text(text = "(${activity.relativeTime})")
    }
}

private val ActivityAction.icon
    get() = when (this) {
        ActivityAction.BLOCK -> Icons.Default.Block
        ActivityAction.BOUNCED -> Icons.Default.Warning
        ActivityAction.FORWARD -> Icons.AutoMirrored.Filled.ForwardToInbox
        ActivityAction.REPLY -> Icons.AutoMirrored.Filled.Reply
    }

private val ActivityAction.color
    get() = when (this) {
        ActivityAction.BLOCK -> SlColor.Red
        ActivityAction.BOUNCED -> SlColor.Amber
        ActivityAction.FORWARD -> SlColor.Green
        ActivityAction.REPLY -> SlColor.Blue
    }

private val Alias.LatestActivity.relativeTime
    get() = timestamp.relativeTimeSpan()