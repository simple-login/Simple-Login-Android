package io.simplelogin.android.ui.home.cell

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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun AliasLatestActivity(activity: Alias.LatestActivity) = key(activity) {
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

        Text(text = activity.contact.email)

        Text(text = "(${activity.relativeTime(LocalContext.current)})")
    }
}