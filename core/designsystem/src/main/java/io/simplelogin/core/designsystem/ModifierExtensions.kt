package io.simplelogin.core.designsystem

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun Modifier.primaryContentBackground(): Modifier =
    this
        .clip(RoundedCornerShape(Spacing.regular))
        .background(color = SlColor.ContentContainerBackgroundColor)

@Composable
fun Modifier.clickableRippleDisabled(onClick: () -> Unit): Modifier =
    this
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )