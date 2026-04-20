package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.Stats
import io.simplelogin.core.model.preferences.AliasCellSelection
import io.simplelogin.core.model.preferences.AliasDisplayInfo
import io.simplelogin.core.model.preferences.AliasOptionsDisplay
import io.simplelogin.core.model.preferences.SwipeAction
import io.simplelogin.core.model.ui.AliasAction
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasList(
    modifier: Modifier = Modifier,
    stats: Stats?,
    aliases: List<Alias>,
    noAliasesMessage: String?,
    fetchError: ApiError?,
    isFetching: Boolean,
    isRefreshing: Boolean,
    optionsDisplay: AliasOptionsDisplay,
    displayInfos: List<AliasDisplayInfo>,
    aliasCellSelection: AliasCellSelection,
    swipeFromStartToEndAction: SwipeAction,
    swipeFromEndToStartAction: SwipeAction,
    onRetry: () -> Unit,
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
                if (total > 0 && lastVisible >= total - 1 && !isFetching && fetchError == null) {
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
            contentPadding = PaddingValues(
                start = Spacing.regular,
                end = Spacing.regular,
                bottom = 80.dp // Avoid FAB
            ),
            state = listState
        ) {
            stats?.let {
                item {
                    StatsGrid(stats = it)
                }
            }

            item {
                Spacer(modifier = Modifier.height(Spacing.regular))
            }

            noAliasesMessage?.let {
                item {
                    if (aliases.isEmpty() && !isRefreshing && !isFetching && fetchError == null) {
                        Text(text = it)
                    }
                }
            }

            itemsIndexed(aliases) { index, alias ->
                AliasRow(
                    modifier = Modifier
                        .clip(
                            RoundedCornerShape(
                                topStart = if (index == 0) Spacing.regular else 0.dp,
                                topEnd = if (index == 0) Spacing.regular else 0.dp,
                                bottomStart = if (index == aliases.lastIndex) Spacing.regular else 0.dp,
                                bottomEnd = if (index == aliases.lastIndex) Spacing.regular else 0.dp,
                            )
                        )
                        .background(SlColor.ContentContainerBackgroundColor),
                    alias = alias,
                    cellSelection = aliasCellSelection,
                    optionsDisplay = optionsDisplay,
                    displayInfos = displayInfos,
                    swipeFromStartToEndAction = swipeFromStartToEndAction,
                    swipeFromEndToStartAction = swipeFromEndToStartAction,
                    onAction = onAction
                )

                if (index < aliases.lastIndex) {
                    HorizontalDivider()
                }
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

            if (fetchError != null) {
                item {
                    RetryButton(error = fetchError, onRetry = onRetry)
                }
            }
        }
    }
}