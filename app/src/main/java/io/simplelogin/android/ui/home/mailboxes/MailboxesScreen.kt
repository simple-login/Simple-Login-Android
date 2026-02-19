package io.simplelogin.android.ui.home.mailboxes

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.DefaultBadge
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.ui.util.UnverifiedBadge
import io.simplelogin.android.ui.util.primaryContentBackground

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailboxesScreen(
    onDismiss: () -> Unit
) = with(hiltViewModel<MailboxesViewModel>()) {
    val context = LocalContext.current
    val state by stateFlow.collectAsState()
    val mailboxes = state.mailboxes
    val fetchError = state.fetchError
    val updateError = state.updateError
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(updateError) {
        updateError?.let {
            snackbarHostState.showSnackbar(it.description(context))
            clearUpdateError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.mailboxes)) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (mailboxes != null) {
                PullToRefreshBox(
                    isRefreshing = state.isFetching,
                    onRefresh = ::fetchMailboxes
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = Spacing.regular)
                            .padding(bottom = Spacing.regular)
                            .primaryContentBackground()
                    ) {
                        itemsIndexed(mailboxes) { index, mailbox ->
                            val topPadding = if (index == 0) 0.dp else Spacing.regular
                            val bottomPadding =
                                if (index == mailboxes.lastIndex) 0.dp else Spacing.regular
                            MailboxRow(
                                modifier = Modifier.padding(
                                    top = topPadding,
                                    bottom = bottomPadding
                                ),
                                mailbox = mailbox,
                                onSetAsDefault = { setAsDefault(mailbox) },
                                onDelete = { delete(mailbox) }
                            )
                            if (index < mailboxes.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            if (state.isUpdating) {
                // Disable user's interaction when loading
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            if (fetchError != null) {
                RetryButton(error = fetchError, onRetry = ::fetchMailboxes)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailboxRow(
    modifier: Modifier,
    mailbox: Mailbox,
    onSetAsDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteAlert by remember { mutableStateOf(false) }
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = mailbox.email,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium,
            color = if (mailbox.verified) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
        )

        if (mailbox.default) {
            Spacer(modifier = Modifier.width(Spacing.regular))
            DefaultBadge()
        }

        if (!mailbox.verified) {
            Spacer(modifier = Modifier.width(Spacing.regular))
            UnverifiedBadge()
        }

        if (!mailbox.default) {
            Spacer(modifier = Modifier.weight(1f))

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = stringResource(R.string.mailbox_options)
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    if (mailbox.verified) {
                        DropdownMenuItem(
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.StarBorder,
                                    contentDescription = null
                                )
                            },
                            text = { Text(text = stringResource(R.string.set_as_default)) },
                            onClick = {
                                showMenu = false
                                onSetAsDefault()
                            }
                        )

                        HorizontalDivider()
                    }

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
                        onClick = {
                            showMenu = false
                            showDeleteAlert = true
                        }
                    )
                }
            }
        }
    }

    if (showDeleteAlert) {
        AlertDialog(
            onDismissRequest = { showDeleteAlert = false },
            title = {
                Text(
                    text = stringResource(
                        id = R.string.delete_mailbox_alert_title, mailbox.email
                    )
                )
            },
            text = {},
            confirmButton = {
                TextButton(onClick = { showDeleteAlert = false }) {
                    Text(text = stringResource(R.string.cancel))
                }

                TextButton(onClick = {
                    showDeleteAlert = false
                    onDelete()
                }) {
                    Text(text = stringResource(R.string.delete))
                }
            }
        )
    }
}