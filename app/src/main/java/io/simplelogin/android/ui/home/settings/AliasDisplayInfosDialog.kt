package io.simplelogin.android.ui.home.settings

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDisplayInfosDialog(
    selection: List<AliasDisplayInfo>,
    onDismiss: () -> Unit,
    onSave: (List<AliasDisplayInfo>) -> Unit
) {
    val selection = rememberSaveable { selection.toMutableStateList() }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Column {
                AliasDisplayInfo.entries.forEach { info ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = selection.contains(info),
                            onCheckedChange = { checked ->
                                if (checked) selection.add(info) else selection.remove(info)
                            }
                        )

                        Text(text = info.title(context))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }

            TextButton(onClick = { onSave(selection) }) {
                Text(stringResource(R.string.save))
            }
        }
    )
}

private fun AliasDisplayInfo.title(context: Context) = when (this) {
    AliasDisplayInfo.CREATION_DATE -> context.getString(R.string.creation_date)
    AliasDisplayInfo.LATEST_ACTIVITY -> context.getString(R.string.latest_activity)
    AliasDisplayInfo.NOTE -> context.getString(R.string.note)
    AliasDisplayInfo.MAILBOXES -> context.getString(R.string.mailboxes)
    AliasDisplayInfo.LAST_14_DAYS -> context.getString(R.string.last_14_days)
}