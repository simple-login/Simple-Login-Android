package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.models.api.Alias
import io.simplelogin.android.ui.util.TextWithInlineIcon

@Composable
fun AliasEmailText(
    alias: Alias,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Medium,
    style: TextStyle = MaterialTheme.typography.titleLarge
) {
    val emailColor =
        if (alias.enabled) LocalContentColor.current else MaterialTheme.colorScheme.outline
    if (alias.pinned) {
        TextWithInlineIcon(
            modifier = modifier,
            text = alias.displayedEmail,
            textColor = emailColor,
            fontWeight = fontWeight,
            style = style,
            icon = painterResource(R.drawable.ic_keep_filled),
            iconSize = style.fontSize,
            iconTint = MaterialTheme.colorScheme.primary
        )
    } else {
        Text(
            modifier = modifier,
            text = alias.displayedEmail,
            color = emailColor,
            fontWeight = fontWeight,
            style = style
        )
    }
}