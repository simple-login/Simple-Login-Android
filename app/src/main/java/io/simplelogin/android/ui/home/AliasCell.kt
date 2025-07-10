package io.simplelogin.android.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.ui.home.dialog.DeleteAliasDialog
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class AliasCellAction {
    data class ViewDetails(val id: Int): AliasCellAction()
    data class ViewContacts(val id: Int): AliasCellAction()
    data class CopyAliasAddress(val email: String): AliasCellAction()
    data class EnableAlias(val id: Int): AliasCellAction()
    data class DisableAlias(val id: Int): AliasCellAction()
    data class DeleteAlias(val id: Int): AliasCellAction()
}

@Composable
fun AliasCell(
    modifier: Modifier = Modifier,
    id: Int,
    email: String,
    mailboxes: List<String>,
    note: String,
    forward: Int,
    reply: Int,
    block: Int,
    isActive: Boolean,
    onAction: (AliasCellAction) -> Unit
) {
    val mailboxes = mailboxes.joinToString(separator = ", ")
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val closeMenuAndSendAction: (AliasCellAction) -> Unit = {
        scope.launch {
            delay(150L) // Wait for ripple animation to finish
            showMenu = false
            onAction(it)
        }
    }

    Row(
        modifier = modifier
    ) {
        Column {
            Text(text = email)

            Text(text = mailboxes)

            if ((forward + reply + block) > 0) {
                AliasCellActivities(
                    forward = forward,
                    reply = reply,
                    block = block
                )
            }

        }

        Spacer(modifier = Modifier.weight(1f))

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.alias_options)
                )
            }

            AliasCellDropdownMenu(
                showMenu = showMenu,
                id = id,
                email = email,
                isActive = isActive,
                onAction = { action ->
                    when (action) {
                        is AliasCellAction.DeleteAlias -> {
                            showMenu = false
                            showDeleteDialog = true
                        }
                        else -> {
                            closeMenuAndSendAction(action)
                        }
                    }

                },
                onDismiss = { showMenu = false }
            )
        }
    }

    if (showDeleteDialog) {
        DeleteAliasDialog(
            aliasEmail = email,
            onDisableClick = {
                showDeleteDialog = false
                closeMenuAndSendAction(AliasCellAction.DisableAlias(id))
            },
            onDeleteClick = {
                showDeleteDialog = false
                closeMenuAndSendAction(AliasCellAction.DeleteAlias(id))
            },
            onCancelClick = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun AliasCellActivities(
    modifier: Modifier = Modifier,
    forward: Int,
    reply: Int,
    block: Int
) {
    Row(modifier = modifier) {
        AliasCellActivityColumn(
            title = stringResource(R.string.forward),
            titleColor = Color.Green,
            value = forward
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        AliasCellActivityColumn(
            title = stringResource(R.string.reply),
            titleColor = Color.Blue,
            value = reply
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        AliasCellActivityColumn(
            title = stringResource(R.string.block),
            titleColor = Color.Red,
            value = block
        )
    }
}

@Composable
private fun AliasCellActivityColumn(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    value: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = title, color = titleColor)
        Text(text = "$value")
    }
}

@Composable
private fun AliasCellDropdownMenu(
    showMenu: Boolean,
    id: Int,
    email: String,
    isActive: Boolean,
    onAction: (AliasCellAction) -> Unit,
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
            onClick = { onAction(AliasCellAction.ViewDetails(id)) }
        )

        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Contacts,
                    contentDescription = null
                )
            },
            text = { Text(text = stringResource(R.string.view_contacts)) },
            onClick = { onAction(AliasCellAction.ViewContacts(id)) }
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
            onClick = { onAction(AliasCellAction.CopyAliasAddress(email)) }
        )

        HorizontalDivider()

        if (isActive) {
            DropdownMenuItem(
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.DoNotDisturbOn,
                        contentDescription = null
                    )
                },
                text = { Text(text = stringResource(R.string.disable)) },
                onClick = { onAction(AliasCellAction.DisableAlias(id)) }
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
                onClick = { onAction(AliasCellAction.EnableAlias(id)) }
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
            onClick = { onAction(AliasCellAction.DeleteAlias(id)) }
        )
    }
}