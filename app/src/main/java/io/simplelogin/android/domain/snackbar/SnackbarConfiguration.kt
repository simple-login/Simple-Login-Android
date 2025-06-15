package io.simplelogin.android.domain.snackbar

import androidx.compose.material3.SnackbarDuration

data class SnackbarConfiguration(
    val message: String,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
)

data class SnackbarAction(
    val label: String,
    val action: (suspend () -> Unit)?
)