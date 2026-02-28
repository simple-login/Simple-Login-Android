package io.simplelogin.android.ui.home.createalias

import io.simplelogin.android.ui.home.dialog.MailboxesSelectionDialog
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.models.api.Suffix
import io.simplelogin.android.data.models.preferences.DefaultPrefix
import io.simplelogin.android.data.remote.CreateAliasBody
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.RetryButton
import io.simplelogin.android.util.InvalidPrefixReason
import io.simplelogin.android.util.PrefixValidationResult
import io.simplelogin.android.util.validatePrefix
import kotlinx.coroutines.launch
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAliasScreen(
    key: String = rememberSaveable { UUID.randomUUID().toString() },
    viewModel: CreateAliasViewModel = hiltViewModel(key = key),
    onAliasCreated: (Alias) -> Unit,
    onDismiss: () -> Unit
) = with(viewModel) {
    val scope = rememberCoroutineScope()
    var prefix by remember { mutableStateOf(TextFieldValue("")) }
    val prefixValidation = prefix.text.validatePrefix()
    var selectedSuffix by remember { mutableStateOf<Suffix?>(null) }
    var showSuffixDialog by remember { mutableStateOf(false) }
    var selectedMailboxes by remember { mutableStateOf<Set<Mailbox>>(emptySet()) }
    var showMailboxesDialog by remember { mutableStateOf(false) }
    var note by remember { mutableStateOf("") }
    val state by stateFlow.collectAsState()
    val fetchError = state.fetchError

    LaunchedEffect(state.aliasOptions) {
        if (selectedSuffix == null && state.aliasOptions != null) {
            selectedSuffix = state.aliasOptions?.suffixes?.firstOrNull()
        }
    }

    LaunchedEffect(state.defaultPrefix) {
        if (prefix.text.isEmpty() && state.defaultPrefix != null) {
            val prefixText = state.defaultPrefix ?: ""
            prefix = TextFieldValue(text = prefixText, selection = TextRange(prefixText.length))
        }
    }

    LaunchedEffect(state.mailboxes) {
        if (selectedMailboxes.isEmpty() && state.mailboxes?.isNotEmpty() == true) {
            state.mailboxes?.firstOrNull()?.let {
                selectedMailboxes = selectedMailboxes + it
            }
        }
    }

    LaunchedEffect(state.createdAlias) {
        state.createdAlias?.let { onAliasCreated(it) }
    }

    Scaffold(
        modifier = Modifier.wrapContentHeight(),
        topBar = {
            TopAppBar(
                title = {
                    if (state.aliasOptions != null) {
                        Text(text = stringResource(R.string.you_are_about_to_create))
                    }
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
                    if (!state.isLoading && state.fetchError == null)
                        TextButton(
                            enabled = prefixValidation is PrefixValidationResult.Valid,
                            onClick = {
                                selectedSuffix?.let { selectedSuffix ->
                                    viewModel.create(
                                        CreateAliasBody(
                                            prefix = prefix.text,
                                            signedSuffix = selectedSuffix.signature,
                                            mailboxIds = selectedMailboxes.map { it.id },
                                            note = note,
                                            name = null
                                        )
                                    )
                                }
                            }
                        ) {
                            Text(text = stringResource(R.string.create))
                        }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier.padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            if (fetchError != null) {
                RetryButton(
                    error = fetchError,
                    onRetry = {
                        scope.launch { fetchOptions() }
                    }
                )
            }

            if (state.aliasOptions != null && state.mailboxes != null) {
                Column(modifier = Modifier.padding(horizontal = Spacing.regular)) {
                    CustomAliasMainContent(
                        prefix = prefix,
                        randomCharacterCount = state.randomCharacterCount,
                        onPrefixChanged = { prefix = it },
                        prefixValidation = prefixValidation,
                        selectedSuffix = selectedSuffix,
                        onShowSuffixSelection = { showSuffixDialog = true },
                        selectedMailboxes = selectedMailboxes,
                        onShowMailboxesSelection = { showMailboxesDialog = true },
                        note = note,
                        onNoteChanged = { note = it }
                    )
                }
            }

            if (state.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(Unit) {},
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
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

    if (showMailboxesDialog) {
        state.mailboxes?.let { mailboxes ->
            MailboxesSelectionDialog(
                mailboxes = mailboxes,
                initialSelected = selectedMailboxes,
                onSave = {
                    showMailboxesDialog = false
                    selectedMailboxes = it
                },
                onDismiss = { showMailboxesDialog = false },
            )
        }
    }
}

@Composable
private fun CustomAliasMainContent(
    prefix: TextFieldValue,
    randomCharacterCount: Int,
    onPrefixChanged: (TextFieldValue) -> Unit,
    prefixValidation: PrefixValidationResult,
    selectedSuffix: Suffix?,
    onShowSuffixSelection: () -> Unit,
    selectedMailboxes: Set<Mailbox>,
    onShowMailboxesSelection: () -> Unit,
    note: String,
    onNoteChanged: (String) -> Unit
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var showRandomPrefixMenu by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    // Preview
    Text(
        modifier = Modifier.fillMaxWidth(),
        text = buildAnnotatedString {
            if (prefixValidation is PrefixValidationResult.Invalid) {
                withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                    append(prefix.text)
                    selectedSuffix?.value?.let {
                        append(it)
                    }
                }
            } else {
                append(prefix.text)
                selectedSuffix?.value?.let {
                    withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary)) {
                        append(it)
                    }
                }
            }
        },
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center
    )

    // Prefix
    Row(
        modifier = Modifier.padding(top = Spacing.regular),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester),
            value = prefix,
            onValueChange = onPrefixChanged,
            singleLine = true,
            label = { Text(text = stringResource(R.string.prefix)) },
            isError = prefixValidation.isInvalid,
            supportingText = {
                if (prefixValidation is PrefixValidationResult.Invalid) {
                    Text(text = prefixValidation.reason.description(context))
                }
            },
            trailingIcon = {
                Row {
                    if (prefix.text.isNotEmpty()) {
                        IconButton(onClick = { onPrefixChanged(TextFieldValue("")) }) {
                            Icon(
                                imageVector = Icons.Filled.Cancel,
                                contentDescription = stringResource(R.string.clear)
                            )
                        }
                    }

                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(onClick = { showRandomPrefixMenu = true }) {
                            Icon(
                                imageVector = Icons.Filled.Shuffle,
                                contentDescription = stringResource(R.string.random_prefix)
                            )
                        }

                        DropdownMenu(
                            expanded = showRandomPrefixMenu,
                            onDismissRequest = { showRandomPrefixMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.random_word)) },
                                onClick = {
                                    showRandomPrefixMenu = false
                                    val prefix =
                                        DefaultPrefix.RANDOM_WORD.generate(randomCharacterCount)
                                    onPrefixChanged(
                                        TextFieldValue(
                                            text = prefix,
                                            selection = TextRange(prefix.length)
                                        )
                                    )
                                }
                            )

                            DropdownMenuItem(
                                text = { Text(text = stringResource(R.string.random_characters)) },
                                onClick = {
                                    showRandomPrefixMenu = false
                                    val prefix = DefaultPrefix.RANDOM_CHARACTERS.generate(
                                        randomCharacterCount
                                    )
                                    onPrefixChanged(
                                        TextFieldValue(
                                            text = prefix,
                                            selection = TextRange(prefix.length)
                                        )
                                    )
                                }
                            )
                        }
                    }
                }
            }
        )
    }

    // Suffix
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.mediumLarge)
    ) {
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
                .clickable { onShowSuffixSelection() }
        )
    }

    // Mailboxes
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = selectedMailboxes.joinToString("\n") { it.email },
            onValueChange = {},
            readOnly = true,
            label = { Text(text = stringResource(R.string.mailboxes)) },
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
                .clickable { onShowMailboxesSelection() }
        )
    }

    // Notes
    OutlinedTextField(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.mediumLarge),
        value = note,
        onValueChange = onNoteChanged,
        label = { Text(text = stringResource(R.string.note)) },
        minLines = 5
    )
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
