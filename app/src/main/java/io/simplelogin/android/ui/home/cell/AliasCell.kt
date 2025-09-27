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
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.preferences.AliasDisplayInfo
import io.simplelogin.android.data.models.preferences.SwipeAction
import io.simplelogin.android.data.models.ui.AliasAction
import io.simplelogin.android.ui.home.dialog.DeleteAliasDialog
import io.simplelogin.android.ui.home.shared.ActivityStats
import io.simplelogin.android.ui.home.shared.AliasEmailText
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing
import io.simplelogin.android.ui.util.IconContent
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AliasCell(
    modifier: Modifier = Modifier,
    alias: Alias,
    displayInfos: List<AliasDisplayInfo>,
    swipeFromStartToEndAction: SwipeAction,
    swipeFromEndToStartAction: SwipeAction,
    onAction: ((AliasAction) -> Unit)? // null when previewing
) = key(alias, displayInfos, swipeFromStartToEndAction, swipeFromEndToStartAction) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            val action = when (value) {
                SwipeToDismissBoxValue.StartToEnd -> swipeFromStartToEndAction
                SwipeToDismissBoxValue.EndToStart -> swipeFromEndToStartAction
                else -> null
            }

            when (action) {
                SwipeAction.DISABLE_ENABLE ->
                    if (alias.enabled) {
                        onAction?.invoke(AliasAction.Disable(alias))
                    } else {
                        onAction?.invoke(AliasAction.Enable(alias))
                    }

                SwipeAction.PIN_UNPIN ->
                    if (alias.pinned) {
                        onAction?.invoke(AliasAction.Unpin(alias))
                    } else {
                        onAction?.invoke(AliasAction.Pin(alias))
                    }

                SwipeAction.DELETE ->
                    showDeleteDialog = onAction != null

                else -> Unit
            }
            false
        }
    )

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val progress = if (dismissState.targetValue == SwipeToDismissBoxValue.Settled) {
                0f
            } else {
                dismissState.progress
            }

            val backgroundColor = when (direction) {
                SwipeToDismissBoxValue.StartToEnd -> swipeFromStartToEndAction.color(alias)
                SwipeToDismissBoxValue.EndToStart -> swipeFromEndToStartAction.color(alias)
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
                    SwipeToDismissBoxValue.StartToEnd ->
                        swipeFromStartToEndAction.Label(
                            isVisible = dismissState.targetValue != SwipeToDismissBoxValue.Settled,
                            alias = alias
                        )

                    SwipeToDismissBoxValue.EndToStart ->
                        swipeFromEndToStartAction.Label(
                            isVisible = dismissState.targetValue != SwipeToDismissBoxValue.Settled,
                            alias = alias
                        )

                    else -> Color.Transparent

                }
            }
        },
        content = {
            AliasCellContent(
                modifier = Modifier.padding(vertical = Spacing.medium),
                alias = alias,
                displayInfos = displayInfos,
                onAction = { action ->
                    when (action) {
                        is AliasAction.Delete -> showDeleteDialog = true
                        else -> onAction?.invoke(action)
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
                onAction?.invoke(AliasAction.Delete(alias))
            },
            onCancelClick = { showDeleteDialog = false }
        )
    }
}

@Composable
private fun AliasCellContent(
    modifier: Modifier = Modifier,
    alias: Alias,
    displayInfos: List<AliasDisplayInfo>,
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
    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AliasEmailText(
                modifier = Modifier.weight(1f),
                alias = alias
            )

            Box(modifier = modifier) {
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

        if (displayInfos.contains(AliasDisplayInfo.CREATION_DATE)) {
            Text(alias.relativeCreationTime(LocalContext.current))
        }

        if (displayInfos.contains(AliasDisplayInfo.LATEST_ACTIVITY)) {
            alias.latestActivity?.let {
                AliasLatestActivity(it)
            }
        }

        if (displayInfos.contains(AliasDisplayInfo.NOTE) && alias.note != null && alias.note.isNotEmpty()) {
            Text(
                text = alias.note,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (displayInfos.contains(AliasDisplayInfo.MAILBOXES)) {
            Text(text = mailboxes)
        }

        if (displayInfos.contains(AliasDisplayInfo.LAST_14_DAYS) && alias.hasActivities) {
            ActivityStats(
                forward = alias.forwardCount,
                reply = alias.replyCount,
                block = alias.blockCount,
                textStyle = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun SwipeAction.color(alias: Alias): Color =
    when (this) {
        SwipeAction.PIN_UNPIN -> if (alias.pinned) SlColor.Amber else MaterialTheme.colorScheme.primary
        SwipeAction.DISABLE_ENABLE -> if (alias.enabled) Color.Gray else SlColor.Green
        SwipeAction.DELETE -> Color.Red
    }

@Composable
private fun SwipeAction.Label(
    isVisible: Boolean,
    alias: Alias
) {
    when (this) {
        SwipeAction.DISABLE_ENABLE ->
            if (alias.enabled) {
                SwipeActionLabel(
                    isVisible = isVisible,
                    title = stringResource(R.string.disable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.DoNotDisturbOn)
                )
            } else {
                SwipeActionLabel(
                    isVisible = isVisible,
                    title = stringResource(R.string.enable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.CheckCircleOutline)
                )
            }

        SwipeAction.PIN_UNPIN ->
            if (alias.pinned) {
                SwipeActionLabel(
                    isVisible = isVisible,
                    title = stringResource(R.string.unpin),
                    icon = IconContent.PainterContent(painterResource(R.drawable.ic_keep_off))
                )
            } else {
                SwipeActionLabel(
                    isVisible = isVisible,
                    title = stringResource(R.string.pin),
                    icon = IconContent.PainterContent(painterResource(R.drawable.ic_keep))
                )
            }
        SwipeAction.DELETE ->
            SwipeActionLabel(
                isVisible = isVisible,
                title = stringResource(R.string.delete),
                icon = IconContent.ImageVectorContent(Icons.Outlined.Delete)
            )
    }
}

@Composable
fun SwipeActionLabel(
    isVisible: Boolean,
    title: String,
    icon: IconContent
) {
    Column(
        modifier = Modifier
            .padding(horizontal = Spacing.extraLarge)
            .alpha(if (isVisible) 1f else 0f),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (icon) {
            is IconContent.ImageVectorContent ->
                Icon(
                    imageVector = icon.vector,
                    contentDescription = icon.contentDescription,
                    tint = Color.White
                )

            is IconContent.PainterContent ->
                Icon(
                    painter = icon.painter,
                    contentDescription = icon.contentDescription,
                    tint = Color.White
                )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = Color.White
        )
    }
}