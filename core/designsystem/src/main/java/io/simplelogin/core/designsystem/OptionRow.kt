package io.simplelogin.core.designsystem

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Constraints
import io.simplelogin.core.designsystem.theme.Spacing

@Composable
fun <T> OptionRow(
    modifier: Modifier = Modifier,
    paddingValues: PaddingValues = PaddingValues(),
    title: String,
    description: @Composable (T) -> Unit,
    options: Array<T>,
    selected: T,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Layout(
        modifier = modifier
            .clickable { expanded = true }
            .padding(paddingValues),
        content = {
            Text(text = title)
            Box {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AnimatedContent(
                        targetState = selected,
                        transitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) { targetSelected ->
                        description(targetSelected)
                    }
                    Icon(
                        painter = painterResource(R.drawable.ic_selector),
                        contentDescription = null
                    )
                }
                DropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            trailingIcon = {
                                if (option == selected) {
                                    Icon(
                                        imageVector = Icons.Outlined.Check,
                                        contentDescription = null
                                    )
                                }
                            },
                            text = { description(option) },
                            onClick = {
                                onSelect(option)
                                expanded = false
                            }
                        )
                        if (index < options.lastIndex) {
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    ) { measurables, constraints ->
        val (titleM, descM) = measurables
        val gap = Spacing.small.roundToPx()
        val descPlaceable = descM.measure(Constraints())
        val titleNaturalWidth = titleM.maxIntrinsicWidth(constraints.maxHeight)
        val useColumn = titleNaturalWidth + gap + descPlaceable.width > constraints.maxWidth

        val titlePlaceable = titleM.measure(
            if (useColumn) {
                Constraints(maxWidth = constraints.maxWidth)
            } else {
                Constraints(
                    minWidth = 0,
                    maxWidth = constraints.maxWidth - descPlaceable.width - gap
                )
            }
        )

        if (useColumn) {
            layout(constraints.maxWidth, titlePlaceable.height + gap + descPlaceable.height) {
                titlePlaceable.place(0, 0)
                descPlaceable.place(0, titlePlaceable.height + gap)
            }
        } else {
            val height = maxOf(titlePlaceable.height, descPlaceable.height)
            layout(constraints.maxWidth, height) {
                titlePlaceable.place(0, (height - titlePlaceable.height) / 2)
                descPlaceable.place(
                    constraints.maxWidth - descPlaceable.width,
                    (height - descPlaceable.height) / 2
                )
            }
        }
    }
}
