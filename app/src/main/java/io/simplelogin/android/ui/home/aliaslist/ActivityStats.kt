package io.simplelogin.android.ui.home.aliaslist

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Block
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun ActivityStats(
    modifier: Modifier = Modifier,
    forward: Int,
    reply: Int,
    block: Int,
    textStyle: TextStyle
) {
    Row(
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        ActivityColumn(
            icon = Icons.AutoMirrored.Filled.Send,
            title = stringResource(R.string.forward),
            titleColor = SlColor.Green,
            value = forward,
            textStyle = textStyle
        )

        VerticalDivider(modifier = Modifier.padding(Spacing.medium))

        ActivityColumn(
            icon = Icons.AutoMirrored.Filled.Reply,
            title = stringResource(R.string.reply),
            titleColor = SlColor.Blue,
            value = reply,
            textStyle = textStyle
        )

        VerticalDivider(modifier = Modifier.padding(Spacing.medium))

        ActivityColumn(
            icon = Icons.Default.Block,
            title = stringResource(R.string.block),
            titleColor = SlColor.Red,
            value = block,
            textStyle = textStyle
        )
    }
}

@Composable
private fun ActivityColumn(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    title: String,
    titleColor: Color,
    value: Int,
    textStyle: TextStyle
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Icon(
                modifier = Modifier.size(textStyle.fontSize.value.dp),
                imageVector = icon,
                tint = titleColor,
                contentDescription = null
            )

            Spacer(modifier = Modifier.size((textStyle.fontSize.value / 3).dp))

            Text(
                text = title,
                color = titleColor,
                fontWeight = FontWeight.Medium,
                style = textStyle
            )
        }
        Text(text = "$value", fontWeight = FontWeight.Bold)
    }
}