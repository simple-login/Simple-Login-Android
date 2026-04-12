package io.simplelogin.android.ui.home.aliasdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.core.common.relativeDateTime
import io.simplelogin.android.core.designsystem.theme.SlColor
import io.simplelogin.android.core.designsystem.theme.Spacing
import io.simplelogin.android.core.model.api.ActivityAction
import io.simplelogin.android.core.model.api.AliasActivity
import io.simplelogin.android.data.models.ui.ActivityUiAction

@Composable
fun AliasActivityRow(
    modifier: Modifier = Modifier,
    clipShape: Shape,
    activity: AliasActivity,
    onAction: (ActivityUiAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    fun closeMenuAndSendAction(action: ActivityUiAction) {
        showMenu = false
        onAction(action)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(clipShape)
            .background(SlColor.ContentContainerBackgroundColor)
            .clickable { showMenu = true }
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

        Spacer(modifier = Modifier.weight(1f))

        Box {
            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.view_options)
                    )
                }
            }

            DropdownMenu(
                expanded = showMenu,
                onDismissRequest = { showMenu = false }
            ) {
                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.copy_reverse_alias_with_display_name)) },
                    onClick = { closeMenuAndSendAction(ActivityUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME) }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.copy_reverse_alias_without_display_name)) },
                    onClick = { closeMenuAndSendAction(ActivityUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME) }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.AlternateEmail,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.copy_email_address)) },
                    onClick = { closeMenuAndSendAction(ActivityUiAction.COPY_ADDRESS) }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null
                        )
                    },
                    text = { Text(text = stringResource(R.string.open_default_email_client)) },
                    onClick = { closeMenuAndSendAction(ActivityUiAction.OPEN_DEFAULT_EMAIL_CLIENT) }
                )
            }
        }
    }
}