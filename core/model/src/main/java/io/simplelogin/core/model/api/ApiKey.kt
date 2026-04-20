package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@[JvmInline Serializable]
value class ApiKey(
    @SerializedName("api_key") val value: String
)