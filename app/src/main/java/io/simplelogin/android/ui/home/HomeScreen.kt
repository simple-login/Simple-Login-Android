package io.simplelogin.android.ui.home

import androidx.activity.compose.BackHandler
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DockedSearchBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.api.generateRandomAlias
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.cell.AliasCell
import io.simplelogin.android.ui.theme.Spacing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    modifier: Modifier,
    onOpenDrawer: () -> Unit,
    onViewDetails: (Int) -> Unit,
    onViewContacts: (Int) -> Unit
) = with(hiltViewModel<HomeViewModel>()) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior()
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }

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
                    scrollBehavior = scrollBehavior,
                    onOpenDrawer = onOpenDrawer,
                    onSearchClick = { isSearching = true }
                )
            }
        }
    ) { innerPadding ->
        AliasesList(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            stats = Stats(aliasCount = 123, blockCount = 44, forwardCount = 13, replyCount = 83),
            onAction = {
                when (it) {
                    is AliasAction.ViewDetails -> onViewDetails(it.id)

                    is AliasAction.ViewContacts -> onViewContacts(it.id)

                    is AliasAction.CopyEmailAddress -> copyAliasAddress(it.email)

                    is AliasAction.Disable -> {

                    }

                    is AliasAction.Enable -> {

                    }

                    is AliasAction.Pin -> {

                    }

                    is AliasAction.Unpin -> {

                    }

                    is AliasAction.Delete -> {

                    }
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NormalTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onOpenDrawer: () -> Unit,
    onSearchClick: () -> Unit
) {
    MediumTopAppBar(
        title = {
            Text(stringResource(R.string.all_aliases))
        },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Filled.Menu,
                    contentDescription = stringResource(R.string.open_menu)
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.search)
                )
            }

            IconButton(onClick = { }) {
                Icon(
                    imageVector = Icons.Default.FilterAlt,
                    contentDescription = stringResource(R.string.filter_options)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SearchTopAppBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onExitSearch: () -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(false) }
    DockedSearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding(),
        shape = SearchBarDefaults.dockedShape,
        expanded = expanded,
        onExpandedChange = {
            expanded = it
            if (!it) {
                onExitSearch()
            }
        },
        inputField = {
            SearchBarDefaults.InputField(
                query = query,
                onQueryChange = onQueryChange,
                onSearch = { expanded = false },
                expanded = expanded,
                onExpandedChange = { expanded = it },
                placeholder = { Text(stringResource(R.string.search_all_aliases)) },
                leadingIcon = {
                    IconButton(onClick = onExitSearch) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.exit_search)
                        )
                    }
                }
            )
        },
        content = {
            Column { }
        }
    )
}

@Composable
private fun AliasesList(
    modifier: Modifier = Modifier,
    stats: Stats,
    onAction: (AliasAction) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = Spacing.regular)
    ) {
        item {
            StatsGrid(stats = stats)
        }

        item {
            Spacer(modifier = Modifier.height(Spacing.regular))
        }

        items(100) {
            AliasCell(
                alias = generateRandomAlias(),
                onAction = onAction
            )
            HorizontalDivider()
        }
    }
}