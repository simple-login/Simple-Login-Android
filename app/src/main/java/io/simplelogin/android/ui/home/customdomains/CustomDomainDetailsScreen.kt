package io.simplelogin.android.ui.home.customdomains

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.designsystem.description
import io.simplelogin.android.models.api.CustomDomain
import io.simplelogin.android.ui.home.dialog.EditTextDialog
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.SettingsFooter
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.ToggleOption
import io.simplelogin.android.ui.util.clickableRippleDisabled
import io.simplelogin.android.ui.util.primaryContentBackground
import io.simplelogin.android.util.relativeDateTime

@SuppressLint("LocalContextGetResourceValueCall")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainDetailsScreen(
    domain: CustomDomain,
    apiKeyValue: String,
    onViewDeletedAliases: () -> Unit,
    onDismiss: () -> Unit
) {
    val viewModel =
        hiltViewModel(key = "custom_domain_${domain.id}") { factory: CustomDomainDetailsViewModel.Factory ->
            factory.create(domain = domain, apiKeyValue = apiKeyValue)
        }
    val context = LocalContext.current
    val state by viewModel.stateFlow.collectAsState()
    val domain = state.domain
    var showEditDisplayNameDialog by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState by remember { mutableStateOf(SnackbarHostState()) }

    LaunchedEffect(state.updateError) {
        state.updateError?.let {
            snackbarHostState.showSnackbar(message = it.description(context))
            viewModel.clearUpdateError()
        }
    }

    LaunchedEffect(state.isUpdated) {
        val message = context.getString(R.string.updated_successfully)
        if (state.isUpdated) {
            snackbarHostState.showSnackbar(message = message)
            viewModel.clearIsUpdated()
        }
    }

    Scaffold(
        containerColor = SlColor.BackgroundColor,
        topBar = {
            TopAppBar(
                title = {
                    Text(text = buildAnnotatedString {
                        append(domain.domainName)
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
                    })
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
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            CustomDomainDetailList(
                domain = domain,
                onEditDisplayName = { showEditDisplayNameDialog = true },
                onToggleCatchAll = viewModel::updateCatchAll,
                onToggleRandomPrefixGeneration = viewModel::updateRandomPrefixGeneration,
                onViewDeletedAliases = onViewDeletedAliases
            )

            AnimatedVisibility(
                visible = state.isUpdating,
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
        }
    }

    if (showEditDisplayNameDialog) {
        EditTextDialog(
            title = domain.domainName,
            label = stringResource(R.string.display_name),
            initialValue = domain.name,
            onSave = {
                showEditDisplayNameDialog = false
                viewModel.updateDisplayName(it)
            },
            onDismiss = { showEditDisplayNameDialog = false }
        )
    }
}

@Composable
private fun CustomDomainDetailList(
    domain: CustomDomain,
    onEditDisplayName: () -> Unit,
    onToggleCatchAll: (Boolean) -> Unit,
    onToggleRandomPrefixGeneration: (Boolean) -> Unit,
    onViewDeletedAliases: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .padding(horizontal = Spacing.regular)
            .padding(bottom = Spacing.regular)
    ) {
        item {
            SettingsHeader(text = stringResource(R.string.created_at))
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .padding(Spacing.regular),
                text = domain.creationTimestamp.relativeDateTime(LocalContext.current)
            )
            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.default_display_name))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .primaryContentBackground()
                    .clickable { onEditDisplayName() }
                    .padding(Spacing.regular),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val displayName = domain.name
                if (displayName != null) {
                    Text(text = displayName)
                    Spacer(modifier = Modifier.weight(1f))
                    CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                        IconButton(onClick = onEditDisplayName) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = stringResource(R.string.edit_display_name)
                            )
                        }
                    }
                } else {
                    Text(
                        text = stringResource(R.string.create_display_name),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            SettingsFooter(
                text = stringResource(
                    R.string.default_display_name_footer,
                    domain.domainName
                )
            )

            SettingsSpacer()
        }

        item {
            SettingsHeader(text = stringResource(R.string.catch_all_header))

            ToggleOption(
                modifier = Modifier.primaryContentBackground(),
                paddingValues = PaddingValues(Spacing.regular),
                checked = domain.catchAll,
                onCheckedChange = { onToggleCatchAll(it) },
                title = stringResource(R.string.catch_all),
                description = stringResource(R.string.catch_all_footer, domain.domainName)
            )

            SettingsSpacer()
        }

        item {
            ToggleOption(
                modifier = Modifier.primaryContentBackground(),
                paddingValues = PaddingValues(Spacing.regular),
                checked = domain.randomPrefixGeneration,
                onCheckedChange = { onToggleRandomPrefixGeneration(it) },
                title = stringResource(R.string.random_prefix_generation),
                description = stringResource(R.string.random_prefix_generation_footer)
            )

            SettingsSpacer()
        }

        item {
            Row(
                modifier = Modifier
                    .primaryContentBackground()
                    .clickable { onViewDeletedAliases() }
                    .padding(Spacing.regular),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = stringResource(R.string.deleted_aliases))
                Spacer(modifier = Modifier.weight(1f))
                Icon(imageVector = Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}