package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class DomainLite(
    @SerializedName("domain") val name: String,
    @SerializedName("is_custom") val isCustom: Boolean
)