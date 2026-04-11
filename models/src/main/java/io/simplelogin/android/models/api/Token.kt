package io.simplelogin.android.models.api

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("token") val value: String
)
