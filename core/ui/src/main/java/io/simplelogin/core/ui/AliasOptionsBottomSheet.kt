package io.simplelogin.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.outlined.CheckCircleOutline
import androidx.compose.material.icons.outlined.Contacts
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DoNotDisturbOn
import androidx.compose.material.icons.outlined.RemoveRedEye
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import io.simplelogin.core.designsystem.IconResource
import io.simplelogin.core.designsystem.theme.Spacing
import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.ui.AliasAction
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import io.simplelogin.core.designsystem.R as DesignSystemR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AliasOptionBottomSheet(
    alias: Alias,
    aliasDetails: Boolean,
    onDismiss: () -> Unit,
    onAction: (AliasAction) -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(
            skipPartiallyExpanded = true
        ),
        dragHandle = null
    ) {
        AliasEmailText(
            modifier = Modifier.padding(
                horizontal = Spacing.large,
                vertical = Spacing.regular
            ),
            alias = alias
        )

        if (!aliasDetails) {
            AliasOptionRow(
                icon = IconResource.ImageVector(Icons.Outlined.RemoveRedEye),
                text = stringResource(R.string.view_details),
                onClick = { onAction(AliasAction.ViewDetails(alias)) }
            )

            AliasOptionRow(
                icon = IconResource.ImageVector(Icons.Outlined.Contacts),
                text = stringResource(R.string.view_contacts),
                onClick = { onAction(AliasAction.ViewContacts(alias)) }
            )

            HorizontalDivider()
        }

        AliasOptionRow(
            icon = IconResource.ImageVector(Icons.Default.ContentCopy),
            text = stringResource(R.string.copy_alias_address),
            onClick = { onAction(AliasAction.CopyEmailAddress(alias)) }
        )

        AliasOptionRow(
            icon = IconResource.ImageVector(Icons.Default.PhoneAndroid),
            text = stringResource(R.string.enter_full_screen),
            onClick = { onAction(AliasAction.EnterFullScreen(alias)) }
        )

        HorizontalDivider()

        if (alias.enabled) {
            AliasOptionRow(
                icon = IconResource.ImageVector(Icons.Outlined.DoNotDisturbOn),
                text = stringResource(R.string.disable),
                onClick = { onAction(AliasAction.Disable(alias)) }
            )
        } else {
            AliasOptionRow(
                icon = IconResource.ImageVector(Icons.Outlined.CheckCircleOutline),
                text = stringResource(R.string.enable),
                onClick = { onAction(AliasAction.Enable(alias)) }
            )
        }

        if (alias.pinned) {
            AliasOptionRow(
                icon = IconResource.Painter(painterResource(DesignSystemR.drawable.ic_keep_off)),
                text = stringResource(R.string.unpin),
                onClick = { onAction(AliasAction.Unpin(alias)) }
            )
        } else {
            AliasOptionRow(
                icon = IconResource.Painter(painterResource(DesignSystemR.drawable.ic_keep)),
                text = stringResource(R.string.pin),
                onClick = { onAction(AliasAction.Pin(alias)) }
            )
        }

        HorizontalDivider()

        AliasOptionRow(
            icon = IconResource.ImageVector(Icons.Outlined.Delete),
            text = stringResource(R.string.delete),
            color = Color.Red,
            onClick = { onAction(AliasAction.Delete(alias)) }
        )
    }
}

@Composable
private fun AliasOptionRow(
    icon: IconResource,
    text: String,
    color: Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = {
                scope.launch {
                    delay(150L) // Wait for ripple animation to finish
                    onClick()
                }
            })
            .padding(
                horizontal = Spacing.large,
                vertical = Spacing.regular
            ),
        horizontalArrangement = Arrangement.Start
    ) {
        when (icon) {
            is IconResource.ImageVector ->
                Icon(
                    imageVector = icon.value,
                    contentDescription = null,
                    tint = color
                )

            is IconResource.Painter ->
                Icon(
                    painter = icon.value,
                    contentDescription = null,
                    tint = color
                )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))
        Text(
            text = text,
            color = color
        )
    }
}
