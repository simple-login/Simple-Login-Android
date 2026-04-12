package io.simplelogin.android.ui.home.settings.device

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.core.designsystem.title
import io.simplelogin.android.core.model.preferences.AliasDisplayInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasDisplayInfosDialog(
    selection: List<AliasDisplayInfo>,
    onSelectionChange: (List<AliasDisplayInfo>) -> Unit,
    onDismiss: () -> Unit
) {
    val selection = rememberSaveable { selection.toMutableStateList() }
    val context = LocalContext.current
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.alias_information)) },
        text = {
            Column {
                AliasDisplayInfo.entries.forEach { info ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (selection.contains(info)) {
                                    selection.remove(info)
                                } else {
                                    selection.add(info)
                                }
                                onSelectionChange(selection)
                            },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selection.contains(info),
                            onCheckedChange = { checked ->
                                if (checked) selection.add(info) else selection.remove(info)
                            }
                        )

                        Text(info.title(context))
                    }
                }
            }
        },
        confirmButton = {}
    )
}