package io.simplelogin.feature.customdomains

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.core.common.relativeDateTime
import io.simplelogin.core.designsystem.RetryButton
import io.simplelogin.core.designsystem.SettingsHeader
import io.simplelogin.core.designsystem.clickableRippleDisabled
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.CustomDomain

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainDeletedAliasesScreen(
    domain: CustomDomain,
    apiKeyValue: String,
    onDismiss: () -> Unit
) {
    val viewModel =
        hiltViewModel(
            key = "custom_domain_deleted_aliases_${domain.id}"
        ) { factory: CustomDomainDeletedAliasesViewModel.Factory ->
            factory.create(domain = domain, apiKeyValue = apiKeyValue)
        }
    val context = LocalContext.current
    val state by viewModel.stateFlow.collectAsState()

    Scaffold(
        containerColor = SlColor.BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = domain.domainName)
                },
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
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = if (state.aliases?.isEmpty() == true) Alignment.Center else Alignment.TopCenter
        ) {
            state.aliases?.let { aliases ->
                if (aliases.isEmpty()) {
                    Text(
                        text = stringResource(R.string.no_deleted_aliases),
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = Spacing.regular)
                            .padding(bottom = Spacing.regular)
                    ) {
                        item {
                            SettingsHeader(
                                text = pluralStringResource(
                                    R.plurals.number_of_deleted_aliases,
                                    aliases.count(),
                                    aliases.count()
                                )
                            )
                        }
                        itemsIndexed(aliases) { index, alias ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(
                                        RoundedCornerShape(
                                            topStart = if (index == 0) Spacing.regular else 0.dp,
                                            topEnd = if (index == 0) Spacing.regular else 0.dp,
                                            bottomStart = if (index == aliases.lastIndex) Spacing.regular else 0.dp,
                                            bottomEnd = if (index == aliases.lastIndex) Spacing.regular else 0.dp
                                        )
                                    )
                                    .background(SlColor.ContentContainerBackgroundColor)
                                    .padding(Spacing.regular),
                                horizontalAlignment = Alignment.Start
                            ) {
                                Text(text = alias.alias)
                                Text(
                                    text = domain.creationTimestamp.relativeDateTime(context),
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }

                            if (index < aliases.lastIndex) {
                                HorizontalDivider()
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(
                visible = state.isFetching,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickableRippleDisabled(onClick = {}),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.fetchError?.let {
                RetryButton(error = it, onRetry = viewModel::fetchDeletedAliases)
            }
        }
    }
}
