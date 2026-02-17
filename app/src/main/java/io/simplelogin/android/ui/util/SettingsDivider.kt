package io.simplelogin.android.ui.util

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun SettingsDivider() {
    HorizontalDivider(modifier = Modifier.padding(horizontal = Spacing.regular))
}