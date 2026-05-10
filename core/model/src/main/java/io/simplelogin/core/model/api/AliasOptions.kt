package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName

data class AliasOptions(
    @SerializedName("can_create") val canCreate: Boolean,
    @SerializedName("prefixSuggestion") val prefixSuggestion: String?,
    @SerializedName("suffixes") val suffixes: List<Suffix>
)

data class Suffix(
    @SerializedName("suffix") val value: String,
    @SerializedName("signed_suffix") val signature: String,
    @SerializedName("is_custom") val isCustom: Boolean,
    @SerializedName("is_premium") val isPremium: Boolean
)
