package io.simplelogin.android.models.api

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