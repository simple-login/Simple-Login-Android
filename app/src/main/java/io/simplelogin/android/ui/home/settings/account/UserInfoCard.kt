package io.simplelogin.android.ui.home.settings.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import io.simplelogin.android.R
import io.simplelogin.android.models.api.UserInfo
import io.simplelogin.android.ui.theme.SlColor
import io.simplelogin.android.ui.theme.Spacing

@Composable
fun UserInfoCard(
    modifier: Modifier = Modifier,
    userInfo: UserInfo,
    onClick: () -> Unit,
    editMenu: @Composable (() -> Unit)? = null
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clickable(onClick = onClick)
            .padding(Spacing.regular),
        verticalAlignment = Alignment.CenterVertically
    ) {
        userInfo.profilePictureUrl?.let {
            AsyncImage(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(CircleShape),
                model = it,
                contentScale = ContentScale.Crop,
                contentDescription = stringResource(R.string.profile_picture)
            )
        } ?: Box(
            modifier = Modifier
                .fillMaxHeight()
                .heightIn(min = 48.dp)
                .aspectRatio(1f)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = userInfo.initial.toString(),
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White
            )
        }

        Spacer(modifier = Modifier.width(Spacing.medium))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Default name is the email address so we check if it's equal to email or not
            if (userInfo.name.isNotEmpty() && userInfo.name != userInfo.email) {
                Text(
                    text = userInfo.name,
                    fontWeight = FontWeight.Medium,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = TextUnit.Unspecified,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (userInfo.isPremiumOrTrial) {
                Text(
                    text = stringResource(if (userInfo.inTrial) R.string.premium_trial else R.string.premium),
                    color = if (userInfo.inTrial) SlColor.Green else SlColor.Amber,
                    style = LocalTextStyle.current.copy(
                        lineHeight = TextUnit.Unspecified,
                        platformStyle = PlatformTextStyle(includeFontPadding = false)
                    )
                )
            }

            Text(
                text = userInfo.email,
                style = LocalTextStyle.current.copy(
                    lineHeight = TextUnit.Unspecified,
                    platformStyle = PlatformTextStyle(includeFontPadding = false)
                ),
                overflow = TextOverflow.MiddleEllipsis
            )
        }

        editMenu?.let {
            it()
        }
    }
}