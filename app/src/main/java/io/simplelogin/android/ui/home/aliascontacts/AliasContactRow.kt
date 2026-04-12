package io.simplelogin.android.ui.home.aliascontacts

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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AlternateEmail
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.core.common.relativeDateTime
import io.simplelogin.android.core.model.api.Contact
import io.simplelogin.android.core.model.preferences.ContactCellSelection
import io.simplelogin.android.data.models.ui.ContactUiAction
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun AliasContactRow(
    modifier: Modifier = Modifier,
    clipShape: Shape,
    contact: Contact,
    contactCellSelection: ContactCellSelection,
    onAction: (ContactUiAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    fun closeMenuAndSendAction(action: ContactUiAction) {
        showMenu = false
        onAction(action)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(clipShape)
            .background(SlColor.ContentContainerBackgroundColor)
            .clickable {
                when (contactCellSelection) {
                    ContactCellSelection.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME ->
                        onAction(ContactUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME)

                    ContactCellSelection.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME ->
                        onAction(ContactUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME)

                    ContactCellSelection.COPY_ADDRESS ->
                        onAction(ContactUiAction.COPY_ADDRESS)

                    ContactCellSelection.BLOCK_UNBLOCK ->
                        if (contact.blockForward) {
                            onAction(ContactUiAction.UNBLOCK)
                        } else {
                            onAction(ContactUiAction.BLOCK)
                        }

                    ContactCellSelection.OPEN_DEFAULT_EMAIL_CLIENT ->
                        onAction(ContactUiAction.OPEN_DEFAULT_EMAIL_CLIENT)

                    ContactCellSelection.VIEW_OPTIONS -> showMenu = true
                }
            }
            .padding(horizontal = Spacing.regular)
            .padding(vertical = Spacing.medium),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (contact.blockForward) Icons.Default.Block else Icons.Default.AlternateEmail,
            contentDescription = null,
            tint = if (contact.blockForward) SlColor.Red else MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.width(Spacing.regular))

        Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = contact.email,
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis,
                color = if (contact.blockForward) MaterialTheme.colorScheme.secondary else LocalTextStyle.current.color
            )
            Text(
                text = contact.creationTimestamp.relativeDateTime(LocalContext.current),
                color = MaterialTheme.colorScheme.secondary
            )
        }

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
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                    },
                    text = { Text(text = stringResource(R.string.copy_reverse_alias_with_display_name)) },
                    onClick = { closeMenuAndSendAction(ContactUiAction.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME) }
                )

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                    },
                    text = { Text(text = stringResource(R.string.copy_reverse_alias_without_display_name)) },
                    onClick = { closeMenuAndSendAction(ContactUiAction.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME) }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.AlternateEmail, contentDescription = null)
                    },
                    text = { Text(text = stringResource(R.string.copy_email_address)) },
                    onClick = { closeMenuAndSendAction(ContactUiAction.COPY_ADDRESS) }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = if (contact.blockForward)
                                Icons.Default.ThumbUpOffAlt else Icons.Default.Block,
                            contentDescription = null
                        )
                    },
                    text = {
                        Text(text = stringResource(if (contact.blockForward) R.string.unblock else R.string.block))
                    },
                    onClick = {
                        if (contact.blockForward) {
                            closeMenuAndSendAction(ContactUiAction.UNBLOCK)
                        } else {
                            closeMenuAndSendAction(ContactUiAction.BLOCK)
                        }
                    }
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
                    onClick = { closeMenuAndSendAction(ContactUiAction.OPEN_DEFAULT_EMAIL_CLIENT) }
                )

                HorizontalDivider()

                DropdownMenuItem(
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = null,
                            tint = SlColor.Red
                        )
                    },
                    text = { Text(text = stringResource(R.string.delete), color = SlColor.Red) },
                    onClick = { closeMenuAndSendAction(ContactUiAction.DELETE) }
                )
            }
        }
    }
}
