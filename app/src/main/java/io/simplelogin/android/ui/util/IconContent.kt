package io.simplelogin.android.ui.util

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconContent(contentDescription: String?) {
    data class ImageVectorContent(
        val vector: ImageVector,
        val contentDescription: String? = null
    ) : IconContent(contentDescription)

    data class PainterContent(
        val painter: Painter,
        val contentDescription: String? = null
    ) : IconContent(contentDescription)
}