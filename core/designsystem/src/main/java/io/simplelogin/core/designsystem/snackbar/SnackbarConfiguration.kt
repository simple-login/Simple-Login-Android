package io.simplelogin.core.designsystem.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals

data class SnackbarConfiguration(
    val message: String,
    val type: SnackbarType = SnackbarType.INFORMATION,
    val action: SnackbarAction? = null,
    val duration: SnackbarDuration = SnackbarDuration.Short
) {
    fun toVisuals(): SnackbarVisuals = when (type) {
        SnackbarType.SUCCESS -> SnackbarVisualsSuccess(
            actionLabel = action?.label,
            duration = duration,
            message = message,
            withDismissAction = duration == SnackbarDuration.Indefinite
        )

        SnackbarType.FAILURE -> SnackbarVisualsFailure(
            actionLabel = action?.label,
            duration = duration,
            message = message,
            withDismissAction = duration == SnackbarDuration.Indefinite
        )

        SnackbarType.INFORMATION -> SnackbarVisualsInformation(
            actionLabel = action?.label,
            duration = duration,
            message = message,
            withDismissAction = duration == SnackbarDuration.Indefinite
        )
    }
}

data class SnackbarAction(
    val label: String,
    val action: (suspend () -> Unit)?
)

enum class SnackbarType {
    SUCCESS, FAILURE, INFORMATION
}
