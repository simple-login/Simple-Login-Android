package io.simplelogin.android.core.designsystem

import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector

sealed class IconContent {
    data class ImageVectorContent(
        val vector: ImageVector,
        val contentDescription: String? = null
    ) : IconContent()

    data class PainterContent(
        val painter: Painter,
        val contentDescription: String? = null
    ) : IconContent()
}