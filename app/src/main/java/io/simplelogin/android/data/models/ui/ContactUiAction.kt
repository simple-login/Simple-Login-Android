package io.simplelogin.android.data.models.ui

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material.icons.outlined.BackHand
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.graphics.vector.ImageVector
import io.simplelogin.android.R

enum class ContactUiAction {
    COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME,
    COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME,
    COPY_ADDRESS,
    BLOCK,
    UNBLOCK,
    OPEN_DEFAULT_EMAIL_CLIENT,
    DELETE;

    fun title(context: Context) = when (this) {
        COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME ->
            context.getString(R.string.copy_reverse_alias_with_display_name)

        COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME ->
            context.getString(R.string.copy_reverse_alias_without_display_name)

        COPY_ADDRESS -> context.getString(R.string.copy_alias_address)
        BLOCK -> context.getString(R.string.block)
        UNBLOCK -> context.getString(R.string.unblock)
        OPEN_DEFAULT_EMAIL_CLIENT -> context.getString(R.string.open_default_email_client)
        DELETE -> context.getString(R.string.delete)
    }

    val icon: ImageVector
        get() = when (this) {
            COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME,
            COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME,
            COPY_ADDRESS -> Icons.Default.ContentCopy

            BLOCK -> Icons.Outlined.BackHand
            UNBLOCK -> Icons.Default.ThumbUpOffAlt
            OPEN_DEFAULT_EMAIL_CLIENT -> Icons.Default.ContentCopy
            DELETE -> Icons.Outlined.Delete
        }
}