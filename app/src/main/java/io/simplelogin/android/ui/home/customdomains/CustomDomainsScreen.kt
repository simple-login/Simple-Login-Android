package io.simplelogin.android.ui.home.customdomains

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.ui.util.SettingsFooter
import io.simplelogin.android.ui.util.UnverifiedBadge
import io.simplelogin.android.util.relativeDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainsScreen(
    apiKeyValue: String,
    onViewDetails: (CustomDomain) -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel = hiltViewModel { factory: CustomDomainsViewModel.Factory ->
        factory.create(apiKeyValue)
    }
    val state by viewModel.stateFlow.collectAsState()
    val domains = state.domains

    Scaffold(
        containerColor = SlColor.BackgroundColor,
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.custom_domains)) },
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
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
            contentAlignment = if (domains?.isEmpty() == true) Alignment.Center else Alignment.TopCenter
        ) {
            if (domains != null) {
                if (domains.isEmpty()) {
                    Column(
                        modifier = Modifier.padding(Spacing.regular),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = stringResource(R.string.no_custom_domains_message),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.secondary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            text = buildAnnotatedString {
                                pushLink(LinkAnnotation.Url("https://simplelogin.io/docs/custom-domain/add-domain/"))
                                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                                    append(stringResource(R.string.learn_more))
                                }
                                pop()
                            },
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    DomainList(domains = domains, onViewDetails = onViewDetails)
                }
            }

            if (state.isFetching) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            state.fetchError?.let {
                RetryButton(error = it, onRetry = viewModel::fetchCustomDomains)
            }
        }
    }
}

@Composable
private fun DomainList(
    domains: List<CustomDomain>,
    onViewDetails: (CustomDomain) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = Spacing.regular)
            .padding(bottom = Spacing.regular)
    ) {
        itemsIndexed(domains) { index, domain ->
            DomainRow(
                modifier = Modifier
                    .clip(
                        RoundedCornerShape(
                            topStart = if (index == 0) Spacing.regular else 0.dp,
                            topEnd = if (index == 0) Spacing.regular else 0.dp,
                            bottomStart = if (index == domains.lastIndex) Spacing.regular else 0.dp,
                            bottomEnd = if (index == domains.lastIndex) Spacing.regular else 0.dp
                        )
                    )
                    .background(SlColor.ContentContainerBackgroundColor)
                    .clickable { onViewDetails(domain) }
                    .padding(Spacing.regular),
                domain = domain,
            )
            if (index < domains.lastIndex) {
                HorizontalDivider()
            }
        }

        item {
            SettingsFooter(text = stringResource(R.string.use_web_app_to_add_custom_domains))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DomainRow(
    modifier: Modifier,
    domain: CustomDomain
) {
    val context = LocalContext.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = domain.domainName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (domain.isVerified) MaterialTheme.colorScheme.onBackground else MaterialTheme.colorScheme.secondary
                )

                if (!domain.isVerified) {
                    Spacer(modifier = Modifier.width(Spacing.medium))
                    UnverifiedBadge()
                }
            }

            Text(
                text = buildAnnotatedString {
                    append(domain.creationTimestamp.relativeDateTime(context))
                    append(" • ")
                    if (domain.aliasCount == 0) {
                        append(stringResource(R.string.no_aliases))
                    } else {
                        append(
                            pluralStringResource(
                                R.plurals.number_of_aliases,
                                domain.aliasCount,
                                domain.aliasCount
                            )
                        )
                    }
                },
                color = MaterialTheme.colorScheme.secondary
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null
        )
    }
}