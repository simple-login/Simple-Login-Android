package io.simplelogin.feature.aliasdetail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import io.simplelogin.core.designsystem.R
import io.simplelogin.core.designsystem.theme.SlColor

@Composable
fun AliasDetailPlaceholderScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SlColor.BackgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_logo_with_name),
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = Color.Gray
        )
    }
}
