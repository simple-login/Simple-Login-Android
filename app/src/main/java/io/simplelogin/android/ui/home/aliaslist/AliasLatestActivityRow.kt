package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextOverflow
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.theme.Spacing

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
            overflow = TextOverflow.Ellipsis
        )
        Text(text = "(${activity.relativeTime})")
    }
}