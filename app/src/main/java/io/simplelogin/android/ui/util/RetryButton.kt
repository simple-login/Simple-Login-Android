package io.simplelogin.android.ui.util

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import io.simplelogin.android.R
import io.simplelogin.android.core.designsystem.description
import io.simplelogin.android.core.model.api.ApiError

@Composable
fun RetryButton(
    error: ApiError,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    errorTextAlign: TextAlign = TextAlign.Center
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = error.description(LocalContext.current),
            color = MaterialTheme.colorScheme.error,
            textAlign = errorTextAlign
        )
        TextButton(onClick = onRetry) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Text(text = stringResource(R.string.retry))
        }
    }
}