package io.simplelogin.android.ui.util

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun Modifier.primaryContentBackground(padded: Boolean = true): Modifier =
    this
        .clip(RoundedCornerShape(Spacing.regular))
        .background(color = MaterialTheme.colorScheme.background)
        .padding(if (padded) Spacing.regular else 0.dp)
