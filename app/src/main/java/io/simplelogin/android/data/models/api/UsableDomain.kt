package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class UsableDomain(
    @SerializedName("domain") val name: String,
    @SerializedName("is_custom") val isCustom: Boolean
)
