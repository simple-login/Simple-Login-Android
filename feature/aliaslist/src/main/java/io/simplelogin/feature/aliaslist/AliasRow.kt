package io.simplelogin.feature.aliaslist

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.MonitorHeart
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.simplelogin.core.common.relativeDateTime
import io.simplelogin.core.designsystem.IconContent
import io.simplelogin.core.designsystem.theme.SlColor
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.preferences.AliasCellSelection
import io.simplelogin.core.model.preferences.AliasDisplayInfo
import io.simplelogin.core.model.preferences.AliasOptionsDisplay
import io.simplelogin.core.model.preferences.SwipeAction
import io.simplelogin.core.model.ui.AliasAction
import io.simplelogin.core.ui.ActivityStats
import io.simplelogin.core.ui.AliasEmailText
import io.simplelogin.core.ui.AliasOptionBottomSheet
import io.simplelogin.core.ui.AliasOptionsDropdownMenu
import kotlinx.coroutines.launch
import io.simplelogin.core.designsystem.R as DesignSystemR

@Composable
fun AliasRow(
    modifier: Modifier = Modifier,
    alias: Alias,
    cellSelection: AliasCellSelection,
    optionsDisplay: AliasOptionsDisplay,
    displayInfos: List<AliasDisplayInfo>,
    swipeFromStartToEndAction: SwipeAction,
    swipeFromEndToStartAction: SwipeAction,
    onAction: ((AliasAction) -> Unit)? // null when previewing
) = key(alias, displayInfos, swipeFromStartToEndAction, swipeFromEndToStartAction) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    val dismissState = rememberSwipeToDismissBoxState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(dismissState.targetValue) {
        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
            val action = when (dismissState.targetValue) {
                SwipeToDismissBoxValue.StartToEnd -> swipeFromStartToEndAction
                SwipeToDismissBoxValue.EndToStart -> swipeFromEndToStartAction
                else -> null
            }

            when (action) {
                SwipeAction.DISABLE_ENABLE -> {
                    if (alias.enabled) onAction?.invoke(AliasAction.Disable(alias))
                    else onAction?.invoke(AliasAction.Enable(alias))
                    dismissState.reset()
                }

                SwipeAction.PIN_UNPIN -> {
                    if (alias.pinned) onAction?.invoke(AliasAction.Unpin(alias))
                    else onAction?.invoke(AliasAction.Pin(alias))
                    dismissState.reset()
                }

                SwipeAction.DELETE -> {
                    if (onAction != null) showDeleteDialog = true
                }

                else -> Unit
            }
        }
    }

    SwipeToDismissBox(
        modifier = modifier,
        state = dismissState,
        backgroundContent = {
            val direction = dismissState.dismissDirection
            val isSwiping = dismissState.progress > 0.01f

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

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(if (isSwiping) backgroundColor else Color.Transparent),
                contentAlignment = alignment
            ) {
                if (isSwiping) {
                    val action = if (direction == SwipeToDismissBoxValue.StartToEnd)
                        swipeFromStartToEndAction else swipeFromEndToStartAction

                    action.Label(alias = alias)
                }
            }
        },
        content = {
            AliasCellContent(
                alias = alias,
                cellSelection = cellSelection,
                optionsDisplay = optionsDisplay,
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
                scope.launch {
                    dismissState.reset()
                }
                onAction?.invoke(AliasAction.Delete(alias))
            },
            onCancelClick = {
                showDeleteDialog = false
                scope.launch {
                    dismissState.reset()
                }
            }
        )
    }
}

@Composable
private fun AliasCellContent(
    modifier: Modifier = Modifier,
    alias: Alias,
    cellSelection: AliasCellSelection,
    optionsDisplay: AliasOptionsDisplay,
    displayInfos: List<AliasDisplayInfo>,
    onAction: (AliasAction) -> Unit
) {
    val mailboxes = alias.mailboxes.joinToString(separator = ", ") { it.email }
    var showOptions by remember { mutableStateOf(false) }
    val closeOptionsAndSendAction: (AliasAction) -> Unit = {
        showOptions = false
        onAction(it)
    }

    val optionsIconButton: @Composable () -> Unit = {
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            IconButton(onClick = { showOptions = true }) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.alias_options)
                )
            }
        }
    }

    Column(
        modifier = modifier
            .background(SlColor.ContentContainerBackgroundColor)
            .clickable {
                when (cellSelection) {
                    AliasCellSelection.VIEW_DETAILS -> onAction(
                        AliasAction.ViewDetails(
                            alias
                        )
                    )

                    AliasCellSelection.COPY_EMAIL -> onAction(
                        AliasAction.CopyEmailAddress(
                            alias
                        )
                    )

                    AliasCellSelection.VIEW_OPTIONS -> showOptions = true
                }
            }
            .padding(horizontal = Spacing.regular, vertical = Spacing.medium)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AliasEmailText(
                modifier = Modifier.weight(1f),
                alias = alias
            )

            when (optionsDisplay) {
                AliasOptionsDisplay.BOTTOM_SHEET -> optionsIconButton()

                AliasOptionsDisplay.DROPDOWN_MENU -> {
                    Box {
                        optionsIconButton()
                        AliasOptionsDropdownMenu(
                            showMenu = showOptions,
                            alias = alias,
                            onDismiss = { showOptions = false },
                            onAction = closeOptionsAndSendAction
                        )
                    }
                }
            }
        }

        if (displayInfos.contains(AliasDisplayInfo.CREATION_DATE)) {
            Text(alias.relativeCreationTime(LocalContext.current))
        }

        if (displayInfos.contains(AliasDisplayInfo.LATEST_ACTIVITY)) {
            alias.latestActivity?.let {
                AliasLatestActivityRow(it)
            }
        }

        if (displayInfos.contains(AliasDisplayInfo.LAST_14_DAYS) && alias.hasActivities) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                val fontSize = LocalTextStyle.current.fontSize
                val iconSize = with(LocalDensity.current) { fontSize.toDp() }
                Icon(
                    modifier = Modifier.size(iconSize),
                    imageVector = Icons.Outlined.MonitorHeart,
                    contentDescription = null
                )

                ActivityStats(
                    modifier = Modifier.fillMaxWidth(),
                    showLabel = false,
                    forward = alias.forwardCount,
                    reply = alias.replyCount,
                    block = alias.blockCount
                )
            }
        }

        val note = alias.note
        if (displayInfos.contains(AliasDisplayInfo.NOTE) && !note.isNullOrEmpty()) {
            Text(
                text = note,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (displayInfos.contains(AliasDisplayInfo.MAILBOXES)) {
            Text(text = mailboxes)
        }
    }

    if (showOptions && optionsDisplay == AliasOptionsDisplay.BOTTOM_SHEET) {
        AliasOptionBottomSheet(
            alias = alias,
            aliasDetails = false,
            onDismiss = { showOptions = false },
            onAction = closeOptionsAndSendAction
        )
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
private fun SwipeAction.Label(alias: Alias) {
    when (this) {
        SwipeAction.DISABLE_ENABLE ->
            if (alias.enabled) {
                SwipeActionLabel(
                    isVisible = true,
                    title = stringResource(R.string.disable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.DoNotDisturbOn)
                )
            } else {
                SwipeActionLabel(
                    isVisible = true,
                    title = stringResource(R.string.enable),
                    icon = IconContent.ImageVectorContent(Icons.Outlined.CheckCircleOutline)
                )
            }

        SwipeAction.PIN_UNPIN ->
            if (alias.pinned) {
                SwipeActionLabel(
                    isVisible = true,
                    title = stringResource(R.string.unpin),
                    icon = IconContent.PainterContent(painterResource(DesignSystemR.drawable.ic_keep_off))
                )
            } else {
                SwipeActionLabel(
                    isVisible = true,
                    title = stringResource(R.string.pin),
                    icon = IconContent.PainterContent(painterResource(DesignSystemR.drawable.ic_keep))
                )
            }

        SwipeAction.DELETE ->
            SwipeActionLabel(
                isVisible = true,
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

private fun Alias.relativeCreationTime(context: Context) =
    creationTimestamp.relativeDateTime(context)