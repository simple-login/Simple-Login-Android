package io.simplelogin.feature.mailboxes

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.core.common.relativeDateTime
import io.simplelogin.core.designsystem.DefaultBadge
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.UnverifiedBadge
import io.simplelogin.core.designsystem.description
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Mailbox
import io.simplelogin.core.ui.EditEmailDialog

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MailboxesScreen(
    apiKeyValue: String,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel { factory: MailboxesViewModel.Factory ->
        factory.create(apiKeyValue)
    }

    val context = LocalContext.current
    val state by viewModel.stateFlow.collectAsState()
    val mailboxes = state.mailboxes
    val fetchError = state.fetchError
    val updateError = state.updateError
    val snackbarHostState = remember { SnackbarHostState() }
    var hasInitiallyLoaded by remember { mutableStateOf(false) }
    var showAddMailboxDialog by remember { mutableStateOf(false) }
    var mailboxToDelete by remember { mutableStateOf<Mailbox?>(null) }

    LaunchedEffect(updateError) {
        updateError?.let {
            snackbarHostState.showSnackbar(it.description(context))
            viewModel.clearUpdateError()
        }
    }

    LaunchedEffect(state.isFetching) {
        if (!state.isFetching && state.fetchError == null && hasInitiallyLoaded) {
            snackbarHostState.showSnackbar(context.getString(R.string.updated_successfully))
        }

        if (!state.isFetching && !hasInitiallyLoaded) {
            hasInitiallyLoaded = true
        }
    }

    LaunchedEffect(state.addedMailbox) {
        state.addedMailbox?.let {
            snackbarHostState.showSnackbar(
                context.getString(
                    R.string.mailbox_verification_message,
                    it.email
                )
            )
            viewModel.clearAddedMailbox()
        }
    }

    LaunchedEffect(state.deletedMailbox) {
        state.deletedMailbox?.let {
            snackbarHostState.showSnackbar(
                context.getString(
                    R.string.mailbox_deleted,
                    it.email
                )
            )
            viewModel.clearDeletedMailbox()
        }
    }

    LaunchedEffect(state.newDefaultMailbox) {
        state.newDefaultMailbox?.let {
            snackbarHostState.showSnackbar(
                context.getString(
                    R.string.mailbox_set_as_default,
                    it.email
                )
            )
            viewModel.clearNewDefaultMailbox()
        }
    }

    Scaffold(
        containerColor = SlColor.BackgroundColor,
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
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            AnimatedVisibility(visible = mailboxes != null) {
                FloatingActionButton(onClick = { showAddMailboxDialog = true }) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_mailbox)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (mailboxes != null) {
                PullToRefreshBox(
                    isRefreshing = state.isFetching,
                    onRefresh = viewModel::fetchMailboxes
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = Spacing.regular)
                            .padding(bottom = Spacing.regular),
                        contentPadding = PaddingValues(bottom = 80.dp) // Avoid FAB
                    ) {
                        itemsIndexed(
                            items = mailboxes,
                            key = { _, mailbox -> mailbox.id }
                        ) { index, mailbox ->
                            MailboxRow(
                                modifier = Modifier
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (index == 0) Spacing.regular else 0.dp,
                                            topEnd = if (index == 0) Spacing.regular else 0.dp,
                                            bottomStart = if (index == mailboxes.lastIndex) Spacing.regular else 0.dp,
                                            bottomEnd = if (index == mailboxes.lastIndex) Spacing.regular else 0.dp,
                                        )
                                    )
                                    .background(SlColor.ContentContainerBackgroundColor),
                                mailbox = mailbox,
                                onSetAsDefault = { viewModel.setAsDefault(mailbox) },
                                onDelete = { mailboxToDelete = mailbox }
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
                RetryButton(error = fetchError, onRetry = viewModel::fetchMailboxes)
            }
        }
    }

    if (showAddMailboxDialog) {
        EditEmailDialog(
            title = stringResource(R.string.add_mailbox),
            description = stringResource(R.string.add_mailbox_description),
            ctaTitle = stringResource(R.string.add),
            onAdd = {
                showAddMailboxDialog = false
                viewModel.add(it)
            },
            onDismiss = { showAddMailboxDialog = false }
        )
    }

    mailboxToDelete?.let { mailbox ->
        DeleteMailboxDialog(
            mailboxToDelete = mailbox,
            mailboxes = state.mailboxes ?: listOf(),
            onDelete = {
                mailboxToDelete = null
                viewModel.deleteMailbox(mailbox, it)
            },
            onDismiss = { mailboxToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MailboxRow(
    modifier: Modifier = Modifier,
    mailbox: Mailbox,
    onSetAsDefault: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { showMenu = true }
            .padding(Spacing.regular),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = mailbox.email,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (mailbox.verified) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                )

                if (mailbox.default) {
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    DefaultBadge()
                }

                if (!mailbox.verified) {
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    UnverifiedBadge()
                }
            }

            Text(
                text = buildAnnotatedString {
                    append(mailbox.creationTimestamp.relativeDateTime(LocalContext.current))
                    append(" • ")
                    if (mailbox.aliasCount == 0) {
                        append(stringResource(R.string.no_aliases))
                    } else {
                        append(
                            pluralStringResource(
                                R.plurals.number_of_aliases,
                                mailbox.aliasCount,
                                mailbox.aliasCount
                            )
                        )
                    }
                },
                color = MaterialTheme.colorScheme.secondary
            )
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
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}