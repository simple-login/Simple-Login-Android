package io.simplelogin.android.ui.home.cell

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.dialog.DeleteAliasDialog
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.IconContent
import io.simplelogin.android.ui.util.TextWithInlineIcon
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AliasCell(
    modifier: Modifier = Modifier,
    alias: Alias,
    swipeFromLeftToRightAction: SwipeAction,
    swipeFromRightToLeftAction: SwipeAction,
    onAction: (AliasAction) -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = {
            false
        }
    )

    SwipeToDismissBox(
        modifier = modifier.padding(vertical = Spacing.medium),
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val progress = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
                0f
            } else {
                dismissState.progress
            }

            val backgroundColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> swipeFromRightToLeftAction.color
                SwipeToDismissBoxValue.EndToStart -> swipeFromLeftToRightAction.color
                else -> Color.Transparent
            }

            val alignment = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> Alignment.CenterStart
                SwipeToDismissBoxValue.EndToStart -> Alignment.CenterEnd
                else -> Alignment.Center
            }

            val animatedProgress by animateFloatAsState(
                targetValue = progress
            )

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = alignment
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedProgress)
                        .align(alignment)
                        .background(backgroundColor)
                )
                when (direction) {
                    SwipeToDismissBoxValue.StartToEnd -> swipeFromRightToLeftAction.Label(alias)
                    SwipeToDismissBoxValue.EndToStart -> swipeFromLeftToRightAction.Label(alias)
                    else -> Color.Transparent
                }
            }
        },
        content = {
            AliasCellContent(
                alias = alias,
                onAction = { action ->
                    when (action) {
                        is AliasAction.Delete -> showDeleteDialog = true
                        else -> onAction(action)
                    }
                }
            )
        }
    )

    if (showDeleteDialog) {
        DeleteAliasDialog(
            aliasEmail = alias.email,
            onDeleteClick = {
                showDeleteDialog = false
                onAction(AliasAction.Delete(alias.id))
            },
            onCancelClick = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun AliasCellContent(
    alias: Alias,
    onAction: (AliasAction) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val mailboxes = alias.mailboxes.joinToString(separator = ", ") { it.email }
    val scope = rememberCoroutineScope()
    val closeMenuAndSendAction: (AliasAction) -> Unit = {
        scope.launch {
            delay(150L) // Wait for ripple animation to finish
            showMenu = false
            onAction(it)
        }
    }
    Row {
        Column(modifier = Modifier.weight(1f)) {
            if (alias.pinned) {
                TextWithInlineIcon(
                    text = alias.breakableEmail,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleLarge,
                    icon = painterResource(R.drawable.ic_keep_filled),
                    iconSize = MaterialTheme.typography.titleLarge.fontSize,
                    iconTint = MaterialTheme.colorScheme.primary
                )
            } else {
                Text(
                    text = alias.breakableEmail,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.titleLarge
                )
            }

            alias.note?.let {
                Text(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(text = mailboxes)

            if (alias.hasActivities) {
                AliasCellActivities(
                    forward = alias.forwardCount,
                    reply = alias.replyCount,
                    block = alias.blockCount
                )
            }
        }

        Box {
            IconButton(onClick = { showMenu = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.alias_options)
                )
            }

            AliasCellDropdownMenu(
                showMenu = showMenu,
                alias = alias,
                onAction = closeMenuAndSendAction,
                onDismiss = { showMenu = false }
            )
        }
    }
}

private val SwipeAction.color: Color
    get() = when (this) {
        SwipeAction.DISABLE_ENABLE -> Color.Gray
        SwipeAction.PIN_UNPIN -> Color.Cyan
        SwipeAction.DELETE -> SlColor.Red
    }

@Composable
private fun SwipeAction.Label(alias: Alias) {
    when (this) {
        SwipeAction.DISABLE_ENABLE ->
            if (alias.enabled) {
                SwipeActionLabel(
                    title = stringResource(R.string.disable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.DoNotDisturbOn)
                )
            } else {
                SwipeActionLabel(
                    title = stringResource(R.string.enable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.CheckCircleOutline)
                )
            }

        SwipeAction.PIN_UNPIN ->
            if (alias.pinned) {
                SwipeActionLabel(
                    title = stringResource(R.string.unpin),
                    icon = IconContent.PainterContent(painterResource(R.drawable.ic_keep_off))
                )
            } else {
                SwipeActionLabel(
                    title = stringResource(R.string.pin),
                    icon = IconContent.PainterContent(painterResource(R.drawable.ic_keep))
                )
            }
        SwipeAction.DELETE ->
            SwipeActionLabel(
                title = stringResource(R.string.delete),
                icon = IconContent.ImageVectorContent(Icons.Outlined.Delete)
            )
    }
}

@Composable
fun SwipeActionLabel(
    modifier: Modifier = Modifier,
    title: String,
    icon: IconContent
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (icon) {
            is IconContent.ImageVectorContent ->
                Icon(imageVector = icon.vector, contentDescription = icon.contentDescription)
            is IconContent.PainterContent ->
                Icon(painter = icon.painter, contentDescription = icon.contentDescription)
        }
        Text(text = title)
    }
}