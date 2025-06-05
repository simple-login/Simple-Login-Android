package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

enum class ActivityAction {
    @SerialName("block")
    BLOCK,
    @SerialName("bounced")
    BOUNCED,
    @SerialName("forward")
    FORWARD,
    @SerialName("reply")
    REPLY
}
