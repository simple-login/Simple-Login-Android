package io.simplelogin.android.ui.home.cell

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun AliasCellActivities(
    modifier: Modifier = Modifier,
    forward: Int,
    reply: Int,
    block: Int
) {
    Row(
        modifier = modifier.height(intrinsicSize = IntrinsicSize.Min)
    ) {
        AliasCellActivityColumn(
            title = stringResource(R.string.forward),
            titleColor = SlColor.Green,
            value = forward
        )

        VerticalDivider(modifier = Modifier.padding(Spacing.medium))

        AliasCellActivityColumn(
            title = stringResource(R.string.reply),
            titleColor = SlColor.Blue,
            value = reply
        )

        VerticalDivider(modifier = Modifier.padding(Spacing.medium))

        AliasCellActivityColumn(
            title = stringResource(R.string.block),
            titleColor = SlColor.Red,
            value = block
        )
    }
}

@Composable
private fun AliasCellActivityColumn(
    modifier: Modifier = Modifier,
    title: String,
    titleColor: Color,
    value: Int,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            color = titleColor,
            fontWeight = FontWeight.Medium
        )
        Text(text = "$value")
    }
}