package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class ApiKey(
    @SerializedName("api_key") val value: String
)
