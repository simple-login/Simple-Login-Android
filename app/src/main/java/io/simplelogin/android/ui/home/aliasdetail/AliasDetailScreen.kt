package io.simplelogin.android.ui.home.aliasdetail

import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.ui.home.shared.ActivityStats
import io.simplelogin.android.ui.home.shared.AliasEmailText
import io.simplelogin.android.ui.home.shared.AliasOptionBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDetailScreen(
    alias: Alias
)  {
    val backDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher

    // Must explicitly provide the type of viewModel
    // otherwise it will crash at runtime even though the compiler could infer the type
    val viewModel: AliasDetailViewModel = hiltViewModel { factory: AliasDetailViewModel.Factory ->
        factory.create(alias)
    }

    val state by viewModel.stateFlow.collectAsState()
    var showEditAliasOptions by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { AliasEmailText(alias = alias) },
                navigationIcon = {
                    IconButton(onClick = { backDispatcher?.onBackPressed() }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEditAliasOptions = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = stringResource(R.string.edit_alias)
                        )
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
            item {
                Text(stringResource(R.string.last_14_days))

                ActivityStats(
                    forward = alias.forwardCount,
                    reply = alias.replyCount,
                    block = alias.blockCount,
                    textStyle = MaterialTheme.typography.bodyMedium
                )

                when (state.activitiesState) {
                    is AliasActivitiesState.Loading ->
                        Text("Loading")
                    is AliasActivitiesState.Loaded ->
                        Text("Loaded ${(state.activitiesState as AliasActivitiesState.Loaded).activities.count()}")
                    is  AliasActivitiesState.Error ->
                        Text("Error")
                }
            }
        }
    }

    if (showEditAliasOptions) {
        AliasOptionBottomSheet(
            alias = alias,
            aliasDetails = true,
            onDismiss = { showEditAliasOptions = false },
            onAction = {}
        )
    }
}