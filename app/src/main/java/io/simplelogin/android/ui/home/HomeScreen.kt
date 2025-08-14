package io.simplelogin.android.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasId
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.dialog.FullScreenDialog
import io.simplelogin.android.ui.home.topbar.NormalTopAppBar
import io.simplelogin.android.ui.home.topbar.SearchTopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    onOpenDrawer: () -> Unit,
    onViewDetails: (AliasId) -> Unit,
    onViewContacts: (AliasId) -> Unit
) = with(hiltViewModel<HomeViewModel>()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var fullScreenAlias by rememberSaveable { mutableStateOf<Alias?>(null) }
    val state by stateFlow.collectAsState()

    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    BackHandler {
        if (isSearching) {
            isSearching = false
        } else {
            backDispatcher?.onBackPressed()
        }
    }

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            if (isSearching) {
                SearchTopAppBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onExitSearch = { isSearching = false }
                )
            } else {
                NormalTopAppBar(
                    selectedAliasFilterMode = state.aliasFilterMode,
                    scrollBehavior = scrollBehavior,
                    onOpenDrawer = onOpenDrawer,
                    onSearchClick = { isSearching = true },
                    onSelectAliasFilterMode = ::updateAliasFilterMode
                )
            }
        }
    ) { innerPadding ->
        AliasesList(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            stats = state.stats,
            aliases = state.aliases,
            fetchError = state.fetchError,
            isFetching = state.isFetching,
            isRefreshing = state.isRefreshing,
            displayInfos = state.deviceSettings.aliasDisplayInfos,
            aliasCellSelection = state.deviceSettings.aliasCellSelection,
            swipeFromStartToEndAction = state.deviceSettings.swipeFromLeftToRightAction,
            swipeFromEndToStartAction = state.deviceSettings.swipeFromRightToLeftAction,
            onAction = {
                when (it) {
                    is AliasAction.ViewDetails -> onViewDetails(it.alias.id)

                    is AliasAction.ViewContacts -> onViewContacts(it.alias.id)

                    is AliasAction.CopyEmailAddress -> copyAliasAddress(it.alias.email)

                    is AliasAction.EnterFullScreen -> { fullScreenAlias = it.alias }

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
    }

    fullScreenAlias?.let {
        FullScreenDialog(
            alias = it,
            onDismiss = { fullScreenAlias = null }
        )
    }
}