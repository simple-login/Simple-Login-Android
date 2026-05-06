package io.simplelogin.feature.mailboxes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Mailbox

@Composable
internal fun DeleteMailboxDialog(
    mailboxToDelete: Mailbox,
    mailboxes: List<Mailbox>,
    onDelete: (MailboxDeleteOption) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val options = listOf(MailboxDeleteOption.DeleteAliases) +
            mailboxes.filter { it.verified }.map { MailboxDeleteOption.TransferAliases(it) }
    var selectedOption by remember { mutableStateOf<MailboxDeleteOption?>(null) }
    var showMenu by remember { mutableStateOf(false) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.delete_mailbox)) },
        text = {
            Column {
                Text(
                    text = stringResource(
                        R.string.delete_mailbox_explanation,
                        mailboxToDelete.email
                    )
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                val textColor = if (selectedOption != null) MaterialTheme.colorScheme.onBackground
                else MaterialTheme.colorScheme.secondary
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = selectedOption?.description(context)
                            ?: stringResource(R.string.select_an_option),
                        onValueChange = {},
                        colors = TextFieldDefaults.colors(
                            focusedTextColor = textColor,
                            unfocusedTextColor = textColor
                        ),
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showMenu = true })
                }

                Box {
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        options.forEachIndexed { index, option ->
                            DropdownMenuItem(
                                text = { Text(text = option.description(context)) },
                                trailingIcon = {
                                    if (selectedOption == option) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    selectedOption = option
                                }
                            )

                            if (index < options.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedOption != null,
                onClick = { selectedOption?.let { onDelete(it) } }
            ) {
                Text(text = stringResource(R.string.delete))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        }
    )
}