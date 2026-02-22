package io.simplelogin.android.ui.home.aliasdetail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.dialog.EditTextDialog
import io.simplelogin.android.ui.home.shared.ActivityStats
import io.simplelogin.android.ui.home.shared.AliasEmailText
import io.simplelogin.android.ui.home.shared.AliasOptionBottomSheet
import io.simplelogin.android.ui.home.shared.AliasOptionsDropdownMenu
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.primaryContentBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    alias: Alias,
    onGoBack: () -> Unit,
    onViewContacts: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val viewModel = hiltViewModel { factory: AliasDetailViewModel.Factory ->
        factory.create(alias)
    }

    val state by viewModel.stateFlow.collectAsState()
    var showAliasOptions by remember { mutableStateOf(false) }
    val closeOptionsAndHandleAction: (AliasAction) -> Unit = {
        showAliasOptions = false
    }
    var showAliasNoteEditorDialog by remember { mutableStateOf(false) }

    val optionsIconButton: @Composable () -> Unit = {
        IconButton(onClick = { showAliasOptions = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.edit_alias)
            )
        }
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
                        when (state.devicePreferences.aliasOptionsDisplay) {
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
                )
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                contentPadding = PaddingValues(Spacing.regular)
            ) {
                item {
                    SettingsHeader(text = stringResource(R.string.note))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .primaryContentBackground()
                            .clickable { showAliasNoteEditorDialog = true }
                            .padding(Spacing.regular)) {
                        if (alias.note.isNullOrBlank()) {
                            Text(
                                text = stringResource(R.string.add_note),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(text = alias.note, maxLines = 10)
                        }
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
                }

                item {
                    Text(text = stringResource(R.string.mailboxes))

                    alias.mailboxes.forEach {
                        Text(text = it.email)
                    }
                }

                item {
                    Text(text = stringResource(R.string.last_14_days))

                    ActivityStats(
                        forward = alias.forwardCount,
                        reply = alias.replyCount,
                        block = alias.blockCount,
                        textStyle = MaterialTheme.typography.bodyMedium
                    )

                    when (state.activitiesState) {
                        is AliasActivitiesState.Loading ->
                            CircularProgressIndicator()

                        is AliasActivitiesState.Loaded ->
                            Text("Loaded ${(state.activitiesState as AliasActivitiesState.Loaded).activities.count()}")

                        is AliasActivitiesState.Error ->
                            RetryButton(
                                error = (state.activitiesState as AliasActivitiesState.Error).error,
                                onRetry = { scope.launch { viewModel.getActivities() } })
                    }
                }
            }
        }
    }

    if (showAliasOptions && state.devicePreferences.aliasOptionsDisplay == AliasOptionsDisplay.BOTTOM_SHEET) {
        AliasOptionBottomSheet(
            alias = alias,
            aliasDetails = true,
            onDismiss = { showAliasOptions = false },
            onAction = closeOptionsAndHandleAction
        )
    }

    if (showAliasNoteEditorDialog) {
        EditTextDialog(
            title = alias.email,
            value = alias.note,
            onSave = { showAliasNoteEditorDialog = false },
            onDismiss = { showAliasNoteEditorDialog = false }
        )
    }
}