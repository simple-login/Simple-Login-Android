package io.simplelogin.android.ui.home.cell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.dialog.DeleteAliasDialog
import io.simplelogin.android.ui.theme.Spacing
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AliasCell(
    modifier: Modifier = Modifier,
    alias: Alias,
    onAction: (AliasAction) -> Unit
) {
    val mailboxes = alias.mailboxes.joinToString(separator = ", ") { it.email }
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val closeMenuAndSendAction: (AliasAction) -> Unit = {
        scope.launch {
            delay(150L) // Wait for ripple animation to finish
            showMenu = false
            onAction(it)
        }
    }

    Row(
        modifier = modifier.padding(vertical = Spacing.medium)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row {
                if (alias.pinned) {
                    Icon(
                        painter = painterResource(R.drawable.ic_keep_filled),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = alias.email,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            Text(text = mailboxes)

            if (alias.hasActivities) {
                AliasCellActivities(
                    forward = alias.forwardCount,
                    reply = alias.replyCount,
                    block = alias.blockCount
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.alias_options)
                )
            }

            AliasCellDropdownMenu(
                showMenu = showMenu,
                alias = alias,
                onAction = { action ->
                    when (action) {
                        is AliasAction.Delete -> {
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
            aliasEmail = alias.email,
            onDeleteClick = {
                showDeleteDialog = false
                closeMenuAndSendAction(AliasAction.Delete(alias.id))
            },
            onCancelClick = { showDeleteDialog = false }
        )
    }
}