package io.simplelogin.android.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Abc
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.createalias.CreateAliasScreen
import io.simplelogin.android.ui.home.dialog.FullScreenDialog
import io.simplelogin.android.ui.home.topbar.NormalTopAppBar
import io.simplelogin.android.ui.home.topbar.SearchTopAppBar
import io.simplelogin.android.ui.root.supportsMultiplePanes
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.clickableRippleDisabled

@Composable
fun HomeScreen(
    modifier: Modifier,
    onOpenDrawer: () -> Unit,
    onViewDetails: (Alias) -> Unit,
    onViewContacts: (Alias) -> Unit,
    onCreateAlias: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var showCreateAliasDialog by rememberSaveable { mutableStateOf(false) }
    var fullScreenAlias by rememberSaveable { mutableStateOf<Alias?>(null) }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    BackHandler {
        if (isSearching) {
            isSearching = false
        } else {
            backDispatcher?.onBackPressed()
        }
    }

    Surface(
        modifier = modifier,
        color = SlColor.BackgroundColor
    ) {
        HomeScreenScaffold(
            viewModel = viewModel,
            isSearching = isSearching,
            fabExpanded = fabExpanded,
            onSearchClick = { isSearching = true },
            onExitSearch = { isSearching = false },
            onCollapseFAB = { fabExpanded = !fabExpanded },
            onOpenDrawer = onOpenDrawer,
            onViewDetails = onViewDetails,
            onViewContacts = onViewContacts,
            onEnterFullScreen = { fullScreenAlias = it },
            onCustomAliasClick = {
                if (windowAdaptiveInfo.supportsMultiplePanes()) {
                    showCreateAliasDialog = true
                } else {
                    onCreateAlias()
                }
            }
        )
    }

    fullScreenAlias?.let {
        FullScreenDialog(
            alias = it,
            onDismiss = { fullScreenAlias = null }
        )
    }

    if (showCreateAliasDialog) {
        Dialog(
            onDismissRequest = { showCreateAliasDialog = false },
            properties = DialogProperties(usePlatformDefaultWidth = windowAdaptiveInfo.supportsMultiplePanes())
        ) {
            CreateAliasScreen(onDismiss = { showCreateAliasDialog = false })
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenScaffold(
    viewModel: HomeViewModel,
    isSearching: Boolean,
    fabExpanded: Boolean,
    onSearchClick: () -> Unit,
    onExitSearch: () -> Unit,
    onCollapseFAB: () -> Unit,
    onOpenDrawer: () -> Unit,
    onViewDetails: (Alias) -> Unit,
    onViewContacts: (Alias) -> Unit,
    onEnterFullScreen: (Alias) -> Unit,
    onCustomAliasClick: () -> Unit
) = with(viewModel) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val state by stateFlow.collectAsState()
    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            if (isSearching) {
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onExitSearch = onExitSearch
                )
            } else {
                NormalTopAppBar(
                    selectedAliasFilterMode = state.aliasFilterMode,
                    scrollBehavior = scrollBehavior,
                    onOpenDrawer = onOpenDrawer,
                    onSearchClick = onSearchClick,
                    onSelectAliasFilterMode = ::updateAliasFilterMode
                )
            }
        },
        floatingActionButton = {
            HomeScreenFAB(
                expanded = fabExpanded,
                onClick = onCollapseFAB,
                onRandomByWordClick = {
                    onCollapseFAB()
                    viewModel.randomAlias(RandomMode.WORD)
                },
                onRandomByUuidClick = {
                    onCollapseFAB()
                    viewModel.randomAlias(RandomMode.UUID)
                },
                onCustomAliasClick = {
                    onCollapseFAB()
                    onCustomAliasClick()
                }
            )
        }
    ) { innerPadding ->
        AliasesList(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            stats = state.stats,
            showStats = state.deviceSettings.showStats,
            aliases = state.aliases,
            selectedAliasFilterMode = state.aliasFilterMode,
            fetchError = state.fetchError,
            isFetching = state.isFetching,
            isRefreshing = state.isRefreshing,
            optionsDisplay = state.deviceSettings.aliasOptionsDisplay,
            displayInfos = state.deviceSettings.aliasDisplayInfos,
            aliasCellSelection = state.deviceSettings.aliasCellSelection,
            swipeFromStartToEndAction = state.deviceSettings.swipeFromLeftToRightAction,
            swipeFromEndToStartAction = state.deviceSettings.swipeFromRightToLeftAction,
            onAction = {
                when (it) {
                    is AliasAction.ViewDetails -> onViewDetails(it.alias)

                    is AliasAction.ViewContacts -> onViewContacts(it.alias)

                    is AliasAction.CopyEmailAddress -> copyAliasAddress(it.alias.email)

                    is AliasAction.EnterFullScreen -> onEnterFullScreen(it.alias)

                    is AliasAction.Disable -> toggle(it.alias)

                    is AliasAction.Enable -> toggle(it.alias)

                    is AliasAction.Pin -> pin(it.alias)

                    is AliasAction.Unpin -> unpin(it.alias)

                    is AliasAction.Delete -> delete(it.alias)
                }
            },
            onRetry = ::fetchMoreAliases,
            onFetchMore = ::fetchMoreAliases,
            onRefresh = ::refresh
        )

        if (fabExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickableRippleDisabled(onClick = onCollapseFAB)
            )
        }
    }
}

@Composable
private fun HomeScreenFAB(
    expanded: Boolean,
    onClick: () -> Unit,
    onRandomByWordClick: () -> Unit,
    onRandomByUuidClick: () -> Unit,
    onCustomAliasClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

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
                    title = stringResource(R.string.random_alias_by_uuid),
                    imageVector = Icons.Default.Numbers,
                    onClick = onRandomByUuidClick
                )

                TitledFAB(
                    title = stringResource(R.string.random_alias_by_word),
                    imageVector = Icons.Default.Abc,
                    onClick = onRandomByWordClick
                )

                TitledFAB(
                    title = stringResource(R.string.custom_alias),
                    imageVector = Icons.Default.Edit,
                    onClick = onCustomAliasClick
                )
            }
        }

        FloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.create_new_alias),
                modifier = Modifier.rotate(rotation)
            )
        }
    }
}

@Composable
fun TitledFAB(
    title: String,
    imageVector: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier.clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.mediumLarge)
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = SlColor.BackgroundColor,
            shadowElevation = 2.dp
        ) {
            Text(
                text = title,
                modifier = Modifier.padding(
                    horizontal = Spacing.mediumLarge,
                    vertical = Spacing.medium
                )
            )
        }
        SmallFloatingActionButton(onClick = onClick) {
            Icon(
                imageVector = imageVector,
                contentDescription = title
            )
        }
    }
}