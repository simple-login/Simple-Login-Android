package io.simplelogin.android.domain.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarVisuals
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import io.simplelogin.android.ui.theme.SlColor

class SnackbarVisualsSuccess(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
) : SnackbarVisuals

class SnackbarVisualsFailure(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
): SnackbarVisuals

class SnackbarVisualsInformation(
    override val actionLabel: String?,
    override val duration: SnackbarDuration,
    override val message: String,
    override val withDismissAction: Boolean
): SnackbarVisuals

class SnackbarVisualsColors(
    val containerColor: Color? = null,
    val contentColor: Color? = null,
    val actionColor: Color? = null,
    val actionContentColor: Color? = null,
    val dismissActionContentColor: Color? = null
)

@Composable
fun SnackbarVisuals.colors(): SnackbarVisualsColors =
    when (this) {
        is SnackbarVisualsSuccess -> SnackbarVisualsColors(
            containerColor = SlColor.notificationSuccess,
            contentColor = SlColor.textInverted,
            actionColor = SlColor.textInverted,
            actionContentColor = SlColor.textInverted,
            dismissActionContentColor = SlColor.textInverted
        )

        is SnackbarVisualsFailure -> SnackbarVisualsColors(
            containerColor = SlColor.notificationError,
            contentColor = SlColor.textInverted,
            actionColor = SlColor.textInverted,
            actionContentColor = SlColor.textInverted,
            dismissActionContentColor = SlColor.textInverted
        )

        else -> SnackbarVisualsColors()
    }