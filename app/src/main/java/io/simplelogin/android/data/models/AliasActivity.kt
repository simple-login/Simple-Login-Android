package io.simplelogin.android.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.SerialName

data class AliasActivity(
    @SerialName("action") val action: ActivityAction,
    @SerialName("timestamp") val timestamp: Double,
    @SerialName("from") val from: String,
    @SerialName("to") val to: String,
    @SerialName("reverse_alias") val reverseAlias: String, // "\"marketing at example.com\" <reply@a.b>"
    @SerialName("reverse_alias_address") val reverseAliasAddress: String // "reply@a.b"
)

data class AliasActivities(
    @SerialName("activities") val activities: List<AliasActivity>
)