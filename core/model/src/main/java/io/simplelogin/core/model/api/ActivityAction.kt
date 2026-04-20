package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName

enum class ActivityAction {
    @SerializedName("block")
    BLOCK,

    @SerializedName("bounced")
    BOUNCED,

    @SerializedName("forward")
    FORWARD,

    @SerializedName("reply")
    REPLY
}