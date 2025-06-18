package io.simplelogin.android.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun HomeScreen(modifier: Modifier,
               apiKey: String,
               onOpenDrawer: () -> Unit,
               onAliasClick: (String) -> Unit
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column {
            Text("API key $apiKey")

            Button(onClick = onOpenDrawer) {
                Text("Open drawer")
            }

            Button(onClick = { onAliasClick("Some random alias ID")  }) {
                Text("Alias detail")
            }
        }
    }
}