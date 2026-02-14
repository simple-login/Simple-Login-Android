package io.simplelogin.android.ui.home.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import io.simplelogin.android.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomAliasDialog(
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {},
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = stringResource(R.string.close)
                            )
                        }
                    }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                Text("Custom alias")
            }
        }
    }
}