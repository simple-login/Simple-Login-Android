package io.simplelogin.android.ui.home.aliaslist

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
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.core.model.api.Alias
import io.simplelogin.android.data.models.ui.AliasAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.simplelogin.android.core.designsystem.R as DesignSystemR

@Composable
fun AliasOptionsDropdownMenu(
    showMenu: Boolean,
    alias: Alias,
    onAction: (AliasAction) -> Unit,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val handleSelection: (AliasAction) -> Unit = {
        scope.launch {
            delay(150L) // Wait for ripple animation to finish
            onAction(it)
        }
    }

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
            onClick = { handleSelection(AliasAction.ViewDetails(alias)) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Contacts,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.view_contacts)) },
            onClick = { handleSelection(AliasAction.ViewContacts(alias)) }
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
            onClick = { handleSelection(AliasAction.CopyEmailAddress(alias)) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.PhoneAndroid,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.enter_full_screen)) },
            onClick = { handleSelection(AliasAction.EnterFullScreen(alias)) }
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
                onClick = { handleSelection(AliasAction.Disable(alias)) }
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
                onClick = { handleSelection(AliasAction.Enable(alias)) }
            )
        }

        if (alias.pinned) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_keep_off),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.unpin)) },
                onClick = { handleSelection(AliasAction.Unpin(alias)) }
            )
        } else {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        painter = painterResource(DesignSystemR.drawable.ic_keep),
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.pin)) },
                onClick = { handleSelection(AliasAction.Pin(alias)) }
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
            colors = MenuDefaults.itemColors(
                leadingIconColor = Color.Red,
                textColor = Color.Red
            ),
            onClick = { handleSelection(AliasAction.Delete(alias)) })
    }
}