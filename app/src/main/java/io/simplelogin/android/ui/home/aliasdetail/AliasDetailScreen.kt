package io.simplelogin.android.ui.home.aliasdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.shared.ActivityStats
import io.simplelogin.android.ui.home.shared.AliasEmailText
import io.simplelogin.android.ui.home.shared.AliasOptionBottomSheet
import io.simplelogin.android.ui.home.shared.AliasOptionsDropdownMenu
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.ui.util.isTwoPaneEligible
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    alias: Alias,
    onGoBack: () -> Unit
) {
    val scope = rememberCoroutineScope()

    // Must explicitly provide the type of viewModel
    // otherwise it will crash at runtime even though the compiler could infer the type
    val viewModel: AliasDetailViewModel = hiltViewModel { factory: AliasDetailViewModel.Factory ->
        factory.create(alias)
    }

    val state by viewModel.stateFlow.collectAsState()
    var showAliasOptions by remember { mutableStateOf(false) }
    val closeOptionsAndHandleAction: (AliasAction) -> Unit = {
        showAliasOptions = false
    }
    val optionsIconButton: @Composable () -> Unit = {
        IconButton(onClick = { showAliasOptions = true }) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = stringResource(R.string.edit_alias)
            )
        }
    }

    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AliasEmailText(alias = alias) },
                navigationIcon = {
                    if (!windowSizeClass.isTwoPaneEligible()) {
                        IconButton(onClick = onGoBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.back)
                            )
                        }
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
                .fillMaxSize()
        ) {
            alias.note?.let {
                item {
                    Text(text = it)
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

    if (showAliasOptions && state.devicePreferences.aliasOptionsDisplay == AliasOptionsDisplay.BOTTOM_SHEET) {
        AliasOptionBottomSheet(
            alias = alias,
            aliasDetails = true,
            onDismiss = { showAliasOptions = false },
            onAction = closeOptionsAndHandleAction
        )
    }
}