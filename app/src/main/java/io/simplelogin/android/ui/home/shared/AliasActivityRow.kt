package io.simplelogin.android.ui.home.shared

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ActivityAction
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.util.relativeDateTime

@Composable
fun AliasActivityRow(
    clipShape: Shape,
    activity: AliasActivity
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(clipShape)
            .background(SlColor.ContentContainerBackgroundColor)
            .padding(horizontal = Spacing.regular)
            .padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (activity.action) {
            ActivityAction.BLOCK, ActivityAction.BOUNCED ->
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = stringResource(R.string.block),
                    tint = SlColor.Red
                )

            ActivityAction.REPLY ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.reply),
                    tint = SlColor.Green
                )

            ActivityAction.FORWARD ->
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Reply,
                    contentDescription = stringResource(R.string.forward),
                    tint = SlColor.Blue
                )
        }

        Spacer(modifier = Modifier.width(Spacing.regular))

        Column(horizontalAlignment = Alignment.Start) {
            Text(text = if (activity.action == ActivityAction.REPLY) activity.to else activity.from)
            Text(
                text = activity.timestamp.relativeDateTime(LocalContext.current),
                color = MaterialTheme.colorScheme.secondary
            )
        }
    }
}