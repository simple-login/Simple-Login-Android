package io.simplelogin.android.ui.home.createalias

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.models.api.Mailbox
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.DefaultBadge
import io.simplelogin.android.ui.util.UnverifiedBadge
import io.simplelogin.android.ui.util.clickableRippleDisabled

@Composable
fun MailboxesSelectionDialog(
    title: String,
    description: String? = null,
    mailboxes: List<Mailbox>,
    initialSelectedIds: List<Int>,
    onSave: (List<Mailbox>) -> Unit,
    onDismiss: () -> Unit
) {
    var tempSelection by rememberSaveable { mutableStateOf(initialSelectedIds) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                description?.let {
                    item {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(Spacing.large))
                    }
                }

                itemsIndexed(mailboxes) { index, mailbox ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.medium)
                            .clickableRippleDisabled {
                                if (mailbox.verified) {
                                    tempSelection =
                                        if (tempSelection.contains(mailbox.id) && tempSelection.count() > 1) {
                                            tempSelection - mailbox.id
                                        } else {
                                            tempSelection + mailbox.id
                                        }
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            Checkbox(
                                enabled = mailbox.verified,
                                checked = tempSelection.contains(mailbox.id),
                                onCheckedChange = { checked ->
                                    if (!checked && tempSelection.count() == 1) {
                                        return@Checkbox
                                    }
                                    tempSelection = if (checked) {
                                        tempSelection + mailbox.id
                                    } else {
                                        tempSelection - mailbox.id
                                    }
                                }
                            )
                        }

                        Spacer(modifier = Modifier.width(Spacing.small))

                        Text(
                            modifier = Modifier.weight(1f),
                            text = mailbox.email,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (mailbox.verified) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.width(Spacing.medium))

                        if (mailbox.default) {
                            DefaultBadge()
                        }

                        if (!mailbox.verified) {
                            UnverifiedBadge()
                        }
                    }
                    if (index < mailboxes.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = {
                onSave(mailboxes.filter { tempSelection.contains(it.id) })
            }) {
                Text(text = stringResource(R.string.save))
            }
        }
    )
}