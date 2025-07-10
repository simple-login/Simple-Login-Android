package io.simplelogin.android.ui.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteAliasDialog(
    aliasEmail: String,
    onDisableClick: () -> Unit,
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
            Column {
                TextButton(onClick = onDisableClick) {
                    Text(stringResource(R.string.disable_this_alias))
                }

                TextButton(onClick = onDeleteClick) {
                    Text(stringResource(R.string.delete_this_alias))
                }

                TextButton(onClick = onCancelClick) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    )
}