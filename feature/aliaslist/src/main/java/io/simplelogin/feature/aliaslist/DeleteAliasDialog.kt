package io.simplelogin.feature.aliaslist

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DeleteAliasDialog(
    aliasEmail: String,
    onDeleteClick: () -> Unit,
    onCancelClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancelClick,
        title = { Text(stringResource(R.string.delete_alias_dialog_title)) },
        text = {
            Text(stringResource(R.string.delete_alias_dialog_content, aliasEmail))
        },
        confirmButton = {
            TextButton(onClick = onCancelClick) {
                Text(stringResource(R.string.cancel))
            }

            TextButton(onClick = onDeleteClick) {
                Text(stringResource(R.string.delete))
            }
        }
    )
}
