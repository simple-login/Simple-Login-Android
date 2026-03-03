package io.simplelogin.android.ui.home.aliasactivities

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.home.aliasdetail.AliasActivityRow
import io.simplelogin.android.ui.home.aliaslist.AliasEmailText
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasActivitiesScreen(
    alias: Alias,
    onGoBack: () -> Unit
) {
    val viewModel =
        hiltViewModel(key = "alias_contacts_${alias.id.value}") { factory: AliasActivitiesViewModel.Factory ->
            factory.create(alias)
        }

    val state by viewModel.stateFlow.collectAsState()

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
            containerColor = Color.Transparent
        ) { innerPadding ->
            PullToRefreshBox(
                modifier = Modifier.padding(innerPadding),
                isRefreshing = state.isRefreshing,
                onRefresh = viewModel::refresh
            ) {
                LazyColumn {
                    item {
                        val lastIndex = state.activities.lastIndex
                        state.activities.forEachIndexed { index, activity ->
                            AliasActivityRow(
                                clipShape = RoundedCornerShape(
                                    topStart = if (index == 0) Spacing.regular else 0.dp,
                                    topEnd = if (index == 0) Spacing.regular else 0.dp,
                                    bottomStart = if (index == lastIndex) Spacing.regular else 0.dp,
                                    bottomEnd = if (index == lastIndex) Spacing.regular else 0.dp
                                ),
                                activity = activity,
                                onAction = {
                                    viewModel.handleAction(
                                        activity = activity,
                                        action = it
                                    )
                                }
                            )

                            if (index < lastIndex) {
                                HorizontalDivider()
                            }
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
        }
    }
}