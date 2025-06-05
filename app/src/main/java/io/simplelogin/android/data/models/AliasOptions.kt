package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class AliasOptions(
    @SerialName("can_create") val canCreate: Boolean,
    @SerialName("prefixSuggestion") val prefixSuggestion: String,
    @SerialName("suffixes") val suffixes: List<Suffix>
)

data class Suffix(
    @SerialName("suffix") val value: String,
    @SerialName("signed_suffix") val signature: String,
    @SerialName("is_custom") val isCustom: Boolean,
    @SerialName("is_premium") val isPremium: Boolean
)