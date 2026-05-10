package io.simplelogin.core.designsystem

sealed class IconResource {
    data class ImageVector(val value: androidx.compose.ui.graphics.vector.ImageVector) :
        IconResource()

    data class Painter(val value: androidx.compose.ui.graphics.painter.Painter) : IconResource()
}
