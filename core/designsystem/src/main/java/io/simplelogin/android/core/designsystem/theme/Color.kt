package io.simplelogin.android.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val ProtonPurple = Color(0xFF6D4AFF)

object SlColor {
    val Red = Color(0xFFEB3D7B)
    val Green = Color(0xFF5FC88F)
    val Blue = Color(0xFF4989FF)
    val Amber = Color(0xFFFFC107)

    @get:Composable
    val BackgroundColor: Color
        get() = MaterialTheme.colorScheme.surfaceContainer

    @get:Composable
    val ContentContainerBackgroundColor: Color
        get() = MaterialTheme.colorScheme.background

    @get:Composable
    val textInverted: Color
        get() = if (isSystemInDarkTheme()) Color(0xFF1C1B24) else Color(0xFFFFFFFF)

    @get:Composable
    val notificationSuccess: Color
        get() = if (isSystemInDarkTheme()) Color(0xFF4AB89A) else Color(0xFF007B58)

    @get:Composable
    val notificationError: Color
        get() = if (isSystemInDarkTheme()) Color(0xFFF08FA4) else Color(0xFFCC2D4F)
}