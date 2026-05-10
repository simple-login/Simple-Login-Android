package io.simplelogin.feature.aliascontacts

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.launch
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.SettingsHeader
import io.simplelogin.core.designsystem.TitledFAB
import io.simplelogin.core.designsystem.clickableRippleDisabled
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.ui.AliasEmailText
import io.simplelogin.core.ui.EditEmailDialog

@Suppress("CyclomaticComplexMethod")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasContactsScreen(
    alias: Alias,
    apiKeyValue: String,
    onGoBack: () -> Unit
) {
    val viewModel =
        hiltViewModel(key = "alias_contacts_${alias.id.value}") { factory: AliasContactsViewModel.Factory ->
            factory.create(alias = alias, apiKeyValue = apiKeyValue)
        }

    val state by viewModel.stateFlow.collectAsState()
    val deviceSettings by viewModel.deviceSettings.collectAsState()
    val listState = rememberLazyListState()
    var fabExpanded by rememberSaveable { mutableStateOf(false) }

    val reachedEnd by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
            state.contacts.count() > 0 &&
                lastVisibleItem != null &&
                lastVisibleItem.index >= layoutInfo.totalItemsCount - 1
        }
    }

    LaunchedEffect(reachedEnd) {
        if (reachedEnd) {
            viewModel.loadMoreIfNeed()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.refresh()
    }

    Surface(color = SlColor.BackgroundColor) {
        Scaffold(
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
                    }
                )
            },
            containerColor = Color.Transparent,
            floatingActionButton = {
                ContactsScreenFAB(
                    expanded = fabExpanded,
                    onClick = { fabExpanded = !fabExpanded },
                    onContactPicked = viewModel::createContact,
                    onCreate = viewModel::createContact
                )
            }
        ) { innerPadding ->
            PullToRefreshBox(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize(),
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(Spacing.regular)
                ) {
                    if (state.contacts.isNotEmpty()) {
                        stickyHeader {
                            SettingsHeader(text = stringResource(R.string.contacts))
                        }
                    }

                    if (!state.isRefreshing && !state.isLoadingMore && state.contacts.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_contacts),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    val lastIndex = state.contacts.lastIndex
                    itemsIndexed(state.contacts) { index, contact ->
                        AliasContactRow(
                            clipShape = RoundedCornerShape(
                                topStart = if (index == 0) Spacing.regular else 0.dp,
                                topEnd = if (index == 0) Spacing.regular else 0.dp,
                                bottomStart = if (index == lastIndex) Spacing.regular else 0.dp,
                                bottomEnd = if (index == lastIndex) Spacing.regular else 0.dp
                            ),
                            contact = contact,
                            contactCellSelection = deviceSettings.contactCellSelection,
                            onAction = {
                                viewModel.handleAction(contact = contact, action = it)
                            }
                        )

                        if (index < lastIndex) {
                            HorizontalDivider()
                        }
                    }

                    if (state.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                    }

                    state.error?.let {
                        item {
                            RetryButton(
                                modifier = Modifier.fillMaxWidth(),
                                error = it,
                                onRetry = viewModel::retry
                            )
                        }
                    }
                }
            }

            if (fabExpanded) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickableRippleDisabled(onClick = { fabExpanded = false })
                )
            }
        }
    }
}

@Composable
private fun ContactsScreenFAB(
    expanded: Boolean,
    onClick: () -> Unit,
    onContactPicked: (Uri) -> Unit,
    onCreate: (String) -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    val pickContactsLauncher = rememberLauncherForActivityResult(
        contract = object : ActivityResultContract<Unit, Uri?>() {
            override fun createIntent(context: Context, input: Unit) =
                Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Email.CONTENT_URI)

            override fun parseResult(resultCode: Int, intent: Intent?) =
                if (resultCode == Activity.RESULT_OK) intent?.data else null
        }
    ) { emailUri ->
        emailUri?.let { onContactPicked(it) }
    }

    var showEmailDialog by remember { mutableStateOf(false) }

    Column(
        horizontalAlignment = Alignment.End
    ) {
        AnimatedVisibility(
            visible = expanded,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { it })
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalAlignment = Alignment.End
            ) {
                TitledFAB(
                    title = stringResource(R.string.pick_from_contacts),
                    imageVector = Icons.Default.Contacts,
                    onClick = {
                        onClick()
                        pickContactsLauncher.launch()
                    }
                )

                TitledFAB(
                    title = stringResource(R.string.enter_contact_email),
                    imageVector = Icons.Default.Edit,
                    onClick = {
                        onClick()
                        showEmailDialog = true
                    }
                )
            }
        }

        FloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.create_new_contact),
                modifier = Modifier.rotate(rotation)
            )
        }
    }

    if (showEmailDialog) {
        EditEmailDialog(
            title = stringResource(R.string.enter_contact_email),
            ctaTitle = stringResource(R.string.create),
            onAdd = {
                showEmailDialog = false
                onCreate(it)
            },
            onDismiss = { showEmailDialog = false }
        )
    }
}
