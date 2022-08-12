package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class TemporaryToken(
    @SerializedName("token") val token: String
)