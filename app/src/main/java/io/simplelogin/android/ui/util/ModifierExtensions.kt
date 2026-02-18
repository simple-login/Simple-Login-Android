package io.simplelogin.android.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun Modifier.primaryContentBackground(): Modifier =
    this
        .clip(RoundedCornerShape(Spacing.regular))
        .background(color = MaterialTheme.colorScheme.background)
        .padding(Spacing.regular)

@Composable
fun Modifier.clickableRippleDisabled(onClick: () -> Unit): Modifier =
    this
        .clickable(
            indication = null,
            interactionSource = remember { MutableInteractionSource() },
            onClick = onClick
        )