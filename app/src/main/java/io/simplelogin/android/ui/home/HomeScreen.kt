package io.simplelogin.android.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.core.designsystem.TitledFAB
import io.simplelogin.android.core.designsystem.clickableRippleDisabled
import io.simplelogin.android.core.designsystem.noAliasesMessage
import io.simplelogin.android.core.designsystem.theme.SlColor
import io.simplelogin.android.core.model.api.Alias
import io.simplelogin.android.core.model.api.ApiKey
import io.simplelogin.android.core.model.api.RandomMode
import io.simplelogin.android.core.model.ui.AliasAction
import io.simplelogin.android.core.model.ui.DialogPayload
import io.simplelogin.android.ui.home.aliasdetail.FullScreenDialog
import io.simplelogin.android.ui.home.aliaslist.AliasList
import io.simplelogin.android.ui.home.createalias.CreateAliasScreen
import io.simplelogin.android.ui.home.dialog.EditTextDialog
import io.simplelogin.android.ui.home.topbar.NormalTopAppBar
import io.simplelogin.android.ui.home.topbar.SearchTopAppBar
import io.simplelogin.android.ui.root.supportsMultiplePanes
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun HomeScreen(
    modifier: Modifier,
    apiKeyValue: String,
    onOpenDrawer: () -> Unit,
    onEnterSearch: () -> Unit,
    onViewDetails: (Alias) -> Unit,
    onViewContacts: (Alias) -> Unit,
    onCreateAlias: () -> Unit,
    createdAliasFlow: Flow<Alias> = emptyFlow()
) {
    val viewModel = hiltViewModel { factory: HomeViewModel.Factory ->
        factory.create(apiKeyValue)
    }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var createAliasDialogPayload by rememberSaveable { mutableStateOf<DialogPayload?>(null) }
    var fullScreenAlias by rememberSaveable { mutableStateOf<Alias?>(null) }

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    var fabExpanded by rememberSaveable { mutableStateOf(false) }
    val windowAdaptiveInfo = currentWindowAdaptiveInfo()

    LaunchedEffect(Unit) {
        createdAliasFlow.collect { alias ->
            viewModel.handleCreatedAlias(alias)
        }
    }

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
            onEnterSearch = {
                isSearching = true
                onEnterSearch()
            },
            onExitSearch = { isSearching = false },
            onCollapseFAB = { fabExpanded = !fabExpanded },
            onOpenDrawer = onOpenDrawer,
            onViewDetails = onViewDetails,
            onViewContacts = onViewContacts,
            onEnterFullScreen = { fullScreenAlias = it },
            onCustomAliasClick = {
                if (windowAdaptiveInfo.supportsMultiplePanes()) {
                    createAliasDialogPayload = DialogPayload(ApiKey(viewModel.apiKeyValue))
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

    createAliasDialogPayload?.let { payload ->
        Dialog(
            onDismissRequest = { createAliasDialogPayload = null },
            properties = DialogProperties(usePlatformDefaultWidth = windowAdaptiveInfo.supportsMultiplePanes())
        ) {
            CreateAliasScreen(
                apiKeyValue = payload.apiKey.value,
                onAliasCreated = {
                    createAliasDialogPayload = null
                    viewModel.handleCreatedAlias(it)
                },
                onDismiss = { createAliasDialogPayload = null }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun HomeScreenScaffold(
    viewModel: HomeViewModel,
    isSearching: Boolean,
    fabExpanded: Boolean,
    onEnterSearch: () -> Unit,
    onExitSearch: () -> Unit,
    onCollapseFAB: () -> Unit,
    onOpenDrawer: () -> Unit,
    onViewDetails: (Alias) -> Unit,
    onViewContacts: (Alias) -> Unit,
    onEnterFullScreen: (Alias) -> Unit,
    onCustomAliasClick: () -> Unit
) = with(viewModel) {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var searchQuery by rememberSaveable { mutableStateOf("") }
    val state by stateFlow.collectAsState()
    val searchState by searchStateFlow.collectAsState()

    LaunchedEffect(Unit) {
        snapshotFlow { searchQuery }
            .debounce(300)
            .collect {
                refresh(isSearching = true)
            }
    }

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = Color.Transparent,
        topBar = {
            if (isSearching) {
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = {
                        searchQuery = it
                        updateSearchQuery(query = it)
                    },
                    onExitSearch = {
                        searchQuery = ""
                        updateSearchQuery(query = "")
                        onExitSearch()
                    }
                )
            } else {
                NormalTopAppBar(
                    theme = state.deviceSettings.theme,
                    isPremium = state.userInfo?.isPremium ?: false,
                    inTrial = state.userInfo?.inTrial ?: false,
                    selectedAliasFilterMode = state.aliasFilterMode,
                    scrollBehavior = scrollBehavior,
                    onOpenDrawer = onOpenDrawer,
                    onSearchClick = onEnterSearch,
                    onSelectAliasFilterMode = ::updateAliasFilterMode
                )
            }
        },
        floatingActionButton = {
            AnimatedVisibility(visible = !isSearching) {
                HomeScreenFAB(
                    expanded = fabExpanded,
                    onClick = onCollapseFAB,
                    askForRandomAliasNote = state.deviceSettings.askForRandomAliasNote,
                    onRandomAlias = { mode, note ->
                        onCollapseFAB()
                        viewModel.randomAlias(mode = mode, note = note)
                    },
                    onCustomAliasClick = {
                        onCollapseFAB()
                        onCustomAliasClick()
                    }
                )
            }
        }
    ) { innerPadding ->
        if (isSearching) {
            AliasList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                stats = null,
                aliases = searchState.aliases,
                noAliasesMessage = if (searchQuery.isEmpty()) null else stringResource(
                    R.string.no_search_results,
                    searchQuery
                ),
                fetchError = searchState.fetchError,
                isFetching = searchState.isFetching,
                isRefreshing = searchState.isRefreshing,
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

                        is AliasAction.Disable -> toggle(alias = it.alias, isSearching = true)

                        is AliasAction.Enable -> toggle(alias = it.alias, isSearching = true)

                        is AliasAction.Pin -> pin(alias = it.alias, isSearching = true)

                        is AliasAction.Unpin -> unpin(alias = it.alias, isSearching = true)

                        is AliasAction.Delete -> delete(alias = it.alias, isSearching = true)
                    }
                },
                onRetry = { fetchMoreAliases(isSearching = true) },
                onFetchMore = { fetchMoreAliases(isSearching = true) },
                onRefresh = { refresh(isSearching = true) }
            )
        } else {
            AliasList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                stats = state.displayedStats,
                aliases = state.aliases,
                noAliasesMessage = state.aliasFilterMode.noAliasesMessage(context),
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

                        is AliasAction.Disable -> toggle(alias = it.alias, isSearching = false)

                        is AliasAction.Enable -> toggle(alias = it.alias, isSearching = false)

                        is AliasAction.Pin -> pin(alias = it.alias, isSearching = false)

                        is AliasAction.Unpin -> unpin(alias = it.alias, isSearching = false)

                        is AliasAction.Delete -> delete(alias = it.alias, isSearching = false)
                    }
                },
                onRetry = { fetchMoreAliases(isSearching = false) },
                onFetchMore = { fetchMoreAliases(isSearching = false) },
                onRefresh = { refresh(isSearching = false) }
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
}

@Composable
private fun HomeScreenFAB(
    expanded: Boolean,
    askForRandomAliasNote: Boolean,
    onClick: () -> Unit,
    onRandomAlias: (RandomMode, String?) -> Unit,
    onCustomAliasClick: () -> Unit
) {
    val rotation by animateFloatAsState(
        targetValue = if (expanded) 45f else 0f,
        label = "fab_rotation"
    )

    var selectedRandomMode by rememberSaveable { mutableStateOf<RandomMode?>(null) }

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
                    onClick = {
                        if (askForRandomAliasNote) {
                            selectedRandomMode = RandomMode.UUID
                        } else {
                            onRandomAlias(RandomMode.UUID, null)
                        }
                    }
                )

                TitledFAB(
                    title = stringResource(R.string.random_alias_by_word),
                    imageVector = Icons.Default.Abc,
                    onClick = {
                        if (askForRandomAliasNote) {
                            selectedRandomMode = RandomMode.WORD
                        } else {
                            onRandomAlias(RandomMode.WORD, null)
                        }
                    }
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

    selectedRandomMode?.let { mode ->
        EditTextDialog(
            initialValue = "",
            title = stringResource(R.string.note),
            onSave = {
                selectedRandomMode = null
                onRandomAlias(mode, it)
            },
            onDismiss = { selectedRandomMode = null }
        )
    }
}
