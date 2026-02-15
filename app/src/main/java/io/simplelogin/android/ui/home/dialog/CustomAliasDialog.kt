package io.simplelogin.android.ui.home.dialog

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.util.InvalidPrefixReason
import io.simplelogin.android.util.PrefixValidationResult
import io.simplelogin.android.util.validatePrefix

@Composable
fun CustomAliasDialog(
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        CustomAliasDialogScaffold(
            onDismiss = onDismiss
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CustomAliasDialogScaffold(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var aliasPrefix by rememberSaveable { mutableStateOf("") }
    val prefixValidation = aliasPrefix.validatePrefix()
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
                }
            )
        }
    }
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
