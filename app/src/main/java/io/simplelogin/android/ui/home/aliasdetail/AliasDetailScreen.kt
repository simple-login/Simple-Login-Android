package io.simplelogin.android.ui.home.aliasdetail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import io.simplelogin.android.data.models.api.Alias

@Composable
fun AliasDetailScreen(
    alias: Alias
) {
    val viewModel = hiltViewModel { factory: AliasDetailViewModel.Factory ->
        factory.create(alias)
    }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = viewModel.alias.email)
        }
    }
}