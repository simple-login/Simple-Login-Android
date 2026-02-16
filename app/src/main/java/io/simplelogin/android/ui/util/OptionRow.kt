package io.simplelogin.android.ui.util

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import io.simplelogin.android.R
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun <T> OptionRow(
    title: String,
    description: @Composable (T) -> String,
    options: Array<T>,
    selected: T,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }) {
                expanded = true
            }
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            color = color,
            style = style
        )

        Box {
            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = description(selected),
                    color = color,
                    style = style
                )

                Icon(
                    painter = painterResource(R.drawable.ic_selector),
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        trailingIcon = {
                            if (option == selected) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = null
                                )
                            }
                        },
                        text = { Text(text = description(option)) },
                        onClick = {
                            onSelect(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}