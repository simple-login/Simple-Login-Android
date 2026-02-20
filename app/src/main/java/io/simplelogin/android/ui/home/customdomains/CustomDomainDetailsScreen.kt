package io.simplelogin.android.ui.home.customdomains

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.SettingsFooter
import io.simplelogin.android.ui.util.SettingsHeader
import io.simplelogin.android.ui.util.SettingsSpacer
import io.simplelogin.android.ui.util.ToggleOption
import io.simplelogin.android.ui.util.primaryContentBackground
import io.simplelogin.android.util.relativeDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomDomainDetailsScreen(
    domain: CustomDomain,
    onDismiss: () -> Unit
) {
    val viewModel =
        hiltViewModel(key = "custom_domain_${domain.id}") { factory: CustomDomainDetailsViewModel.Factory ->
            factory.create(domain)
        }
    val domain by viewModel.domainStateFlow.collectAsState()
    var showEditDisplayNameDialog by rememberSaveable { mutableStateOf(false) }

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
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(horizontal = Spacing.regular)
                .padding(bottom = Spacing.regular)
                .padding(innerPadding)
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
                        .clickable { showEditDisplayNameDialog = true }
                        .padding(Spacing.regular),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val displayName = domain.name
                    if (displayName != null) {
                        Text(text = displayName)
                        Spacer(modifier = Modifier.weight(1f))
                        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                            IconButton(onClick = { showEditDisplayNameDialog = true }) {
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
                    onCheckedChange = {},
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
                    onCheckedChange = {},
                    title = stringResource(R.string.random_prefix_generation),
                    description = stringResource(R.string.random_prefix_generation_footer)
                )

                SettingsSpacer()
            }

            item {
                Row(
                    modifier = Modifier
                        .primaryContentBackground()
                        .clickable {}
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

    if (showEditDisplayNameDialog) {
        EditDisplayNameDialog(
            domain = domain,
            onSave = { showEditDisplayNameDialog = false },
            onDismiss = { showEditDisplayNameDialog = false }
        )
    }
}

@Composable
private fun EditDisplayNameDialog(
    domain: CustomDomain,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val domainName = domain.name ?: ""
    var value by remember {
        mutableStateOf(
            TextFieldValue(
                text = domainName,
                selection = TextRange(domainName.length)
            )
        )
    }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(
                    if (domain.name == null) R.string.create_display_name
                    else R.string.edit_display_name
                )
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.focusRequester(focusRequester),
                value = value,
                onValueChange = { value = it },
                trailingIcon = {
                    IconButton(onClick = { value = TextFieldValue(text = "") }) {
                        Icon(
                            imageVector = Icons.Filled.Cancel,
                            contentDescription = stringResource(R.string.clear)
                        )
                    }
                }
            )
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = stringResource(R.string.cancel))
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(value.text) }) {
                Text(text = stringResource(R.string.save))
            }
        }
    )
}