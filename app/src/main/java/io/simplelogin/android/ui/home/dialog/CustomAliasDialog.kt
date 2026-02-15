package io.simplelogin.android.ui.home.dialog

import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.viewModelScope
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.util.InvalidPrefixReason
import io.simplelogin.android.util.PrefixValidationResult
import io.simplelogin.android.util.validatePrefix
import kotlinx.coroutines.launch

@Composable
fun CustomAliasDialog(
    viewModel: CustomAliasDialogViewModel = hiltViewModel(),
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CustomAliasDialogScaffold(
            viewModel = viewModel,
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAliasDialogScaffold(
    viewModel: CustomAliasDialogViewModel,
    onDismiss: () -> Unit
) = with(viewModel) {
    val context = LocalContext.current
    var aliasPrefix by rememberSaveable { mutableStateOf("") }
    val prefixValidation = aliasPrefix.validatePrefix()
    var selectedSuffix by rememberSaveable { mutableStateOf<Suffix?>(null) }
    var showSuffixDialog by rememberSaveable { mutableStateOf(false) }
    val state by stateFlow.collectAsState()
    val fetchError = state.fetchError

    LaunchedEffect(state.aliasOptions) {
        if (selectedSuffix == null && state.aliasOptions != null) {
            selectedSuffix = state.aliasOptions?.suffixes?.firstOrNull()
        }
    }

    Scaffold(
        modifier = Modifier.wrapContentHeight(),
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(R.string.create_new_alias))
                },
                navigationIcon = {
                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Filled.Close,
                            contentDescription = stringResource(R.string.close)
                        )
                    }
                },
                actions = {
                    TextButton(onClick = {}) {
                        Text(text = stringResource(R.string.create))
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = Spacing.regular)
        ) {
            if (fetchError != null) {
                RetryButton(
                    error = fetchError,
                    onRetry = {
                        viewModelScope.launch { fetchOptions() }
                    }
                )
            } else {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = aliasPrefix,
                    onValueChange = { aliasPrefix = it },
                    singleLine = true,
                    label = { Text(text = stringResource(R.string.prefix)) },
                    isError = prefixValidation.isInvalid,
                    supportingText = {
                        if (prefixValidation is PrefixValidationResult.Invalid) {
                            Text(text = prefixValidation.reason.description(context))
                        }
                    },
                    trailingIcon = {
                        if (aliasPrefix.isNotEmpty()) {
                            IconButton(onClick = { aliasPrefix = "" }) {
                                Icon(
                                    imageVector = Icons.Filled.Cancel,
                                    contentDescription = stringResource(R.string.clear)
                                )
                            }
                        }
                    }
                )

                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        modifier = Modifier.fillMaxWidth(),
                        value = selectedSuffix?.value ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(text = stringResource(R.string.suffix)) },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Filled.ArrowDropDown,
                                contentDescription = null
                            )
                        }
                    )

                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showSuffixDialog = true }
                    )
                }
            }
        }
    }

    if (showSuffixDialog) {
        state.aliasOptions?.let { options ->
            SuffixSelectionDialog(
                suffixes = options.suffixes,
                selected = selectedSuffix,
                onSelect = {
                    selectedSuffix = it
                    showSuffixDialog = false
                },
                onDismiss = { showSuffixDialog = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SuffixSelectionDialog(
    suffixes: List<Suffix>,
    selected: Suffix?,
    onSelect: (Suffix) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.select_suffix)) },
        text = {
            LazyColumn {
                itemsIndexed(suffixes) { index, suffix ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = Spacing.medium)
                            .clickable { onSelect(suffix) },
                        horizontalAlignment = Alignment.Start
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
                                RadioButton(
                                    selected = suffix == selected,
                                    onClick = { onSelect(suffix) }
                                )
                            }
                            Spacer(modifier = Modifier.width(Spacing.small))
                            Text(
                                text = suffix.value,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }

                        if (suffix.isCustom) {
                            Text(
                                text = stringResource(R.string.your_domain),
                                color = SlColor.Blue
                            )
                        } else if (suffix.isPremium) {
                            Text(
                                text = stringResource(R.string.premium_domain),
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Text(
                                text = stringResource(R.string.public_domain),
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }

                    if (index < suffixes.lastIndex) {
                        HorizontalDivider()
                    }
                }
            }
        },
        confirmButton = {}
    )
}

private fun InvalidPrefixReason.description(context: Context): String {
    val resId = when (this) {
        InvalidPrefixReason.TWO_CONSECUTIVE_DOTS -> R.string.invalid_prefix_two_consecutive_dots
        InvalidPrefixReason.INVALID_CHARACTER -> R.string.invalid_prefix_invalid_character
        InvalidPrefixReason.DOT_AT_THE_BEGINNING -> R.string.invalid_prefix_dot_at_beginning
        InvalidPrefixReason.DOT_AT_THE_END -> R.string.invalid_prefix_dot_at_end
        InvalidPrefixReason.PREFIX_TOO_LONG -> R.string.invalid_prefix_too_long
        InvalidPrefixReason.PREFIX_EMPTY -> R.string.invalid_prefix_empty
    }
    return context.getString(resId)
}
