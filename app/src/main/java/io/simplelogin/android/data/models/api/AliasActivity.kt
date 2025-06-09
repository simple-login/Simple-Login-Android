package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class AliasActivity(
    @SerializedName("action") val action: ActivityAction,
    @SerializedName("timestamp") val timestamp: Double,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("reverse_alias") val reverseAlias: String, // "\"marketing at example.com\" <reply@a.b>"
    @SerializedName("reverse_alias_address") val reverseAliasAddress: String // "reply@a.b"
)

data class AliasActivities(
    @SerializedName("activities") val activities: List<AliasActivity>
)