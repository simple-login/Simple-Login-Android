package io.simplelogin.android.ui.home

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R

@Composable
fun AliasCell(
    modifier: Modifier = Modifier,
    email: String,
    mailboxes: List<String>,
    note: String,
    forward: Int,
    reply: Int,
    block: Int,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val mailboxes = mailboxes.joinToString(separator = ", ")
    Column (
        modifier = modifier
    ) {
        Text(text = email)

        Text(text = mailboxes)

        if ((forward + reply + block) > 0) {
            AliasCellActivities(
                forward = forward,
                reply = reply,
                block = block
            )
        }
    }
}

@Composable
private fun AliasCellActivities(
    modifier: Modifier = Modifier,
    forward: Int,
    reply: Int,
    block: Int
) {
    Row(modifier = modifier) {
        AliasCellActivityColumn(
            title = stringResource(R.string.forward),
            titleColor = Color.Green,
            value = forward
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        AliasCellActivityColumn(
            title = stringResource(R.string.reply),
            titleColor = Color.Blue,
            value = reply
        )

        VerticalDivider(modifier = Modifier.fillMaxHeight())

        AliasCellActivityColumn(
            title = stringResource(R.string.block),
            titleColor = Color.Red,
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
        Text(text = title, color = titleColor)
        Text(text = "$value")
    }
}