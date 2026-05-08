package io.simplelogin.feature.aliasdetail

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.SettingsHeader
import io.simplelogin.core.designsystem.SettingsSpacer
import io.simplelogin.core.designsystem.primaryContentBackground
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasActivity
import io.simplelogin.core.model.preferences.AliasOptionsDisplay
import io.simplelogin.core.model.ui.ActivityUiAction
import io.simplelogin.core.model.ui.AliasAction
import io.simplelogin.core.ui.ActivityStats
import io.simplelogin.core.ui.AliasActivityRow
import io.simplelogin.core.ui.AliasEmailText
import io.simplelogin.core.ui.AliasOptionBottomSheet
import io.simplelogin.core.ui.AliasOptionsDropdownMenu
import io.simplelogin.core.ui.EditTextDialog
import io.simplelogin.core.ui.MailboxesSelectionDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    alias: Alias,
    apiKeyValue: String,
    onGoBack: () -> Unit,
    onViewContacts: () -> Unit,
    onAliasUpdated: (Alias) -> Unit,
    onViewAllActivities: () -> Unit
) {
    val viewModel =
        hiltViewModel(key = "alias_detail_${alias.id}") { factory: AliasDetailViewModel.Factory ->
            factory.create(aliasIdValue = alias.id.value, apiKeyValue = apiKeyValue)
        }

    val state by viewModel.stateFlow.collectAsState()
    val isLoading = state is AliasDetailScreenState.Loading
    val isLoaded = state is AliasDetailScreenState.Loaded
    val devicePreferences by viewModel.devicePreferencesStateFlow.collectAsState()
    val mailboxesToUpdate by viewModel.mailboxesToUpdateStateFlow.collectAsState()
    var showAliasOptions by remember { mutableStateOf(false) }
    val closeOptionsAndHandleAction: (AliasAction) -> Unit = {
        showAliasOptions = false
    }
    var showNoteEditorDialog by remember { mutableStateOf(false) }
    var showDisplayNameEditorDialog by remember { mutableStateOf(false) }
    val pullToRefreshState = rememberPullToRefreshState()

    val optionsIconButton: @Composable () -> Unit = {
        IconButton(onClick = { showAliasOptions = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.edit_alias)
            )
        }
    }

    LaunchedEffect(alias.id) {
        viewModel.refresh()
    }

    Surface(color = SlColor.BackgroundColor) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                TopAppBar(
                    title = { AliasEmailText(alias = alias) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent),
                    navigationIcon = {
                        IconButton(onClick = onGoBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    actions = {
                        AnimatedVisibility(visible = isLoaded) {
                            when (devicePreferences.aliasOptionsDisplay) {
                                AliasOptionsDisplay.BOTTOM_SHEET -> optionsIconButton()

                                AliasOptionsDisplay.DROPDOWN_MENU -> {
                                    Box {
                                        optionsIconButton()
                                        AliasOptionsDropdownMenu(
                                            showMenu = showAliasOptions,
                                            alias = alias,
                                            onDismiss = { showAliasOptions = false },
                                            onAction = closeOptionsAndHandleAction
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .pullToRefresh(
                        isRefreshing = isLoading,
                        state = pullToRefreshState,
                        enabled = isLoaded,
                        onRefresh = viewModel::refresh
                    ),
                contentAlignment = Alignment.Center
            ) {
                when (state) {
                    is AliasDetailScreenState.Loading -> {}

                    is AliasDetailScreenState.Error ->
                        RetryButton(
                            error = (state as AliasDetailScreenState.Error).error,
                            onRetry = viewModel::refresh
                        )

                    is AliasDetailScreenState.Loaded ->
                        AliasDetailContent(
                            alias = (state as AliasDetailScreenState.Loaded).alias,
                            activities = (state as AliasDetailScreenState.Loaded).activities,
                            hasMoreActivities = (state as AliasDetailScreenState.Loaded).hasMoreActivities,
                            onEditNote = { showNoteEditorDialog = true },
                            onEditDisplayName = { showDisplayNameEditorDialog = true },
                            onEditMailboxes = { viewModel.getMailboxesToUpdate() },
                            onViewContacts = onViewContacts,
                            onActivityAction = { activity, action ->
                                viewModel.handleActivityAction(activity = activity, action = action)
                            },
                            onViewAllActivities = onViewAllActivities
                        )
                }

                PullToRefreshDefaults.Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isLoading,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

    if (showAliasOptions && devicePreferences.aliasOptionsDisplay == AliasOptionsDisplay.BOTTOM_SHEET) {
        AliasOptionBottomSheet(
            alias = alias,
            aliasDetails = true,
            onDismiss = { showAliasOptions = false },
            onAction = closeOptionsAndHandleAction
        )
    }

    if (showNoteEditorDialog) {
        EditTextDialog(
            title = alias.email,
            label = stringResource(R.string.note),
            initialValue = state.alias?.note,
            onSave = { note ->
                showNoteEditorDialog = false
                viewModel.updateNote(note = note, onSuccess = onAliasUpdated)
            },
            onDismiss = { showNoteEditorDialog = false }
        )
    }

    if (showDisplayNameEditorDialog) {
        EditTextDialog(
            title = alias.email,
            label = stringResource(R.string.display_name),
            initialValue = state.alias?.name,
            onSave = { name ->
                showDisplayNameEditorDialog = false
                viewModel.updateName(name = name, onSuccess = onAliasUpdated)
            },
            onDismiss = { showDisplayNameEditorDialog = false }
        )
    }

    mailboxesToUpdate?.let { mailboxes ->
        MailboxesSelectionDialog(
            title = stringResource(R.string.update_mailboxes),
            description = alias.email,
            mailboxes = mailboxes,
            initialSelectedIds = state.alias?.mailboxes?.map { it.id } ?: emptyList(),
            onSave = { mailboxes ->
                viewModel.removeMailboxesToUpdate()
                viewModel.updateMailboxes(mailboxes = mailboxes, onSuccess = onAliasUpdated)
            },
            onDismiss = {
                viewModel.removeMailboxesToUpdate()
            }
        )
    }
}

@Composable
fun AliasDetailContent(
    modifier: Modifier = Modifier,
    alias: Alias,
    activities: List<AliasActivity>,
    hasMoreActivities: Boolean,
    onEditNote: () -> Unit,
    onEditDisplayName: () -> Unit,
    onEditMailboxes: () -> Unit,
    onViewContacts: () -> Unit,
    onActivityAction: (AliasActivity, ActivityUiAction) -> Unit,
    onViewAllActivities: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.regular)
    ) {
        item {
            SettingsHeader(text = stringResource(R.string.note))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .clickable { onEditNote() }
                    .padding(Spacing.regular)) {
                val note = alias.note
                if (note.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.add_note),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(text = note, maxLines = 10)
                }
            }

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.display_name))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .clickable { onEditDisplayName() }
                    .padding(Spacing.regular)) {
                val name = alias.name
                if (name.isNullOrBlank()) {
                    Text(
                        text = stringResource(R.string.add_display_name),
                        color = MaterialTheme.colorScheme.primary
                    )
                } else {
                    Text(text = name, maxLines = 10)
                }
            }

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.mailboxes))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .clickable { onEditMailboxes() }
                    .padding(Spacing.regular),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = alias.mailboxes.joinToString(separator = "\n") { it.email })

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null
                )
            }

            SettingsSpacer()
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .clickable { onViewContacts() }
                    .padding(Spacing.regular),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.contacts))

                Spacer(modifier = Modifier.weight(1f))

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }

            SettingsSpacer()
        }

        if (alias.hasActivities) {
            stickyHeader {
                Surface(color = SlColor.BackgroundColor) {
                    Column {
                        SettingsHeader(text = stringResource(R.string.last_14_days).uppercase())

                        ActivityStats(
                            modifier = Modifier
                                .clip(RoundedCornerShape(percent = 50))
                                .background(MaterialTheme.colorScheme.surfaceBright)
                                .padding(vertical = Spacing.medium),
                            showLabel = true,
                            forward = alias.forwardCount,
                            reply = alias.replyCount,
                            block = alias.blockCount
                        )

                        Spacer(modifier = Modifier.height(Spacing.medium))
                    }
                }
            }

            item {
                activities.forEachIndexed { index, activity ->
                    val lastIndex = activities.lastIndex
                    AliasActivityRow(
                        clipShape = RoundedCornerShape(
                            topStart = if (index == 0) Spacing.regular else 0.dp,
                            topEnd = if (index == 0) Spacing.regular else 0.dp,
                            bottomStart = if (index == lastIndex && !hasMoreActivities) Spacing.regular else 0.dp,
                            bottomEnd = if (index == lastIndex && !hasMoreActivities) Spacing.regular else 0.dp
                        ),
                        activity = activity,
                        onAction = { onActivityAction(activity, it) }
                    )

                    if (index < lastIndex || (index == lastIndex && hasMoreActivities)) {
                        HorizontalDivider()
                    }
                }

                if (hasMoreActivities) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(
                                RoundedCornerShape(
                                    bottomStart = Spacing.regular,
                                    bottomEnd = Spacing.regular
                                )
                            )
                            .clickable { onViewAllActivities() }
                            .background(SlColor.ContentContainerBackgroundColor)
                            .padding(Spacing.regular)
                    ) {
                        Text(text = stringResource(R.string.all_activities))
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}