package io.simplelogin.android.data.models

import com.google.gson.annotations.SerializedName

data class Token(
    @SerializedName("token") val value: String
)
