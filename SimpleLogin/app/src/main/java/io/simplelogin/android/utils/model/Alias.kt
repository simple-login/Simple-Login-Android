package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("note") val note: String?,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int
)

data class AliasArray(
    @SerializedName("aliases") val aliases: List<Alias>
)