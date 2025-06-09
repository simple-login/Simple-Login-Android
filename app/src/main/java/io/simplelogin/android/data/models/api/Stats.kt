package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class Stats(
    @SerializedName("nb_alias") val aliasCount: Int,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int
)
