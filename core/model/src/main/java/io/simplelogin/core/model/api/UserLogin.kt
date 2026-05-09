package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName

data class UserLogin(
    @SerializedName("api_key") val apiKey: String?,
    @SerializedName("email") val email: String,
    @SerializedName("mfa_enabled") val mfaEnabled: Boolean,
    @SerializedName("mfa_key") val mfaKey: String?,
    @SerializedName("name") val name: String
)
