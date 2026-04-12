package io.simplelogin.android.core.model.api

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("token") val value: String
)
