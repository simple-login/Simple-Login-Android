package io.simplelogin.android.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.Stats
import io.simplelogin.android.data.models.preferences.AliasCellSelection
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.cell.AliasCell
import io.simplelogin.android.ui.theme.Spacing
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasesList(
    modifier: Modifier = Modifier,
    stats: Stats,
    aliases: List<Alias>,
    isFetching: Boolean,
    isRefreshing: Boolean,
    aliasCellSelection: AliasCellSelection,
    swipeFromStartToEndAction: SwipeAction,
    swipeFromEndToStartAction: SwipeAction,
    onAction: (AliasAction) -> Unit,
    onFetchMore: () -> Unit,
    onRefresh: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(listState) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisibleItemIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisibleItemIndex to layoutInfo.totalItemsCount
        }
            .distinctUntilChanged()
            .collect { (lastVisible, total) ->
                if (total > 0 && lastVisible >= total - 1 && !isFetching) {
                    onFetchMore()
                }
            }
    }

    PullToRefreshBox(
        modifier = modifier,
        isRefreshing = isRefreshing,
        onRefresh = onRefresh
    ) {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = Spacing.regular),
            state = listState
        ) {
            item {
                StatsGrid(stats = stats)
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.regular))
            }

            items(aliases) { alias ->
                AliasCell(
                    modifier = Modifier.clickable {
                        when (aliasCellSelection) {
                            AliasCellSelection.VIEW_DETAILS -> onAction(AliasAction.ViewDetails(alias.id))
                            AliasCellSelection.COPY_EMAIL -> onAction(AliasAction.CopyEmailAddress(alias.email))
                        }
                    },
                    alias = alias,
                    swipeFromStartToEndAction = swipeFromStartToEndAction,
                    swipeFromEndToStartAction = swipeFromEndToStartAction,
                    onAction = onAction
                )
                HorizontalDivider()
            }

            item {
                AnimatedVisibility(
                    visible = isFetching && !isRefreshing,
                    enter = EnterTransition.None,
                    exit = fadeOut()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .wrapContentSize(Alignment.Center)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }
}