package io.simplelogin.android.ui.home.customdomains

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.ui.theme.SlColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainDetailsScreen(
    domain: CustomDomain,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel { factory: CustomDomainDetailsViewModel.Factory ->
        factory.create(domain)
    }
    val domain by viewModel.domainStateFlow.collectAsState()

    Scaffold(
        containerColor = SlColor.BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(text = domain.domainName) },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { innerPadding ->
        LazyColumn(modifier = Modifier.padding(innerPadding)) {
            item {
                Text(domain.domainName)
            }
        }
    }
}