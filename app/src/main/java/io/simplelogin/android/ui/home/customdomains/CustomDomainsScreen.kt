package io.simplelogin.android.ui.home.customdomains

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainsScreen(
    onDismiss: () -> Unit
) = with(hiltViewModel<CustomDomainsViewModel>()) {
    val state by stateFlow.collectAsState()
    val domains = state.domains
    val fetchError = state.fetchError

    Scaffold(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
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
            contentAlignment = Alignment.Center
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
                    DomainList(domains)
                }
            }

            if (state.isFetching) {
                CircularProgressIndicator()
            }

            if (fetchError != null) {
                RetryButton(error = fetchError, onRetry = ::fetchCustomDomains)
            }
        }
    }
}

@Composable
fun DomainList(domains: List<CustomDomain>) {
    LazyColumn {
        item {
            domains.forEach { domain ->
                Text(domain.domainName)
            }
        }
    }
}