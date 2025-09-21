package io.simplelogin.android.ui.home.aliasdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.data.models.api.Alias

@Composable
fun AliasDetailScreen(
    alias: Alias
)  {
    // Must explicitly provide the type of viewModel
    // otherwise it will crash at runtime even though the compiler could infer the type
    val viewModel: AliasDetailViewModel = hiltViewModel { factory: AliasDetailViewModel.Factory ->
        factory.create(alias)
    }

    val state by viewModel.stateFlow.collectAsState()

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
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