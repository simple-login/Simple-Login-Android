package io.simplelogin.android.ui.home.cell

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.PhoneAndroid
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.ui.AliasAction

@Composable
fun AliasCellDropdownMenu(
    showMenu: Boolean,
    alias: Alias,
    onAction: (AliasAction) -> Unit,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = showMenu,
        onDismissRequest = onDismiss
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.RemoveRedEye,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.view_details)) },
            onClick = { onAction(AliasAction.ViewDetails(alias)) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Contacts,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.view_contacts)) },
            onClick = { onAction(AliasAction.ViewContacts(alias)) }
        )

        HorizontalDivider()

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.ContentCopy,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.copy_alias_address)) },
            onClick = { onAction(AliasAction.CopyEmailAddress(alias)) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PhoneAndroid,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.enter_full_screen)) },
            onClick = { onAction(AliasAction.EnterFullScreen(alias)) }
        )

        HorizontalDivider()

        if (alias.enabled) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DoNotDisturbOn,
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.disable)) },
                onClick = { onAction(AliasAction.Disable(alias)) }
            )
        } else {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.CheckCircleOutline,
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.enable)) },
                onClick = { onAction(AliasAction.Enable(alias)) }
            )
        }

        if (alias.pinned) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_keep_off),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.unpin)) },
                onClick = { onAction(AliasAction.Unpin(alias)) }
            )
        } else {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(R.drawable.ic_keep),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.pin)) },
                onClick = { onAction(AliasAction.Pin(alias)) }
            )
        }

        HorizontalDivider()

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.delete)) },
            onClick = { onAction(AliasAction.Delete(alias)) }
        )
    }
}