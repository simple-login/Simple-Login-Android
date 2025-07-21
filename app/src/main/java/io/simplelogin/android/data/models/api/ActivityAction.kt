package io.simplelogin.android.data.models.api

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ForwardToInbox
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.ui.theme.SlColor

enum class ActivityAction(
    val icon: ImageVector,
    val color: Color
) {
    @SerializedName("block")
    BLOCK(icon = Icons.Default.Block, color = SlColor.Red),

    @SerializedName("bounced")
    BOUNCED(icon = Icons.Default.Warning, color = SlColor.Amber),

    @SerializedName("forward")
    FORWARD(icon = Icons.AutoMirrored.Filled.ForwardToInbox, color = SlColor.Green),

    @SerializedName("reply")
    REPLY(icon = Icons.AutoMirrored.Filled.Reply, color = SlColor.Blue)
}
