package io.simplelogin.android.data.models

import com.google.gson.annotations.SerializedName

typealias ApiKey = String

data class UserLogin(
    @SerializedName("api_key") val apiKey: ApiKey,
    @SerializedName("email") val email: String,
    @SerializedName("mfa_enabled") val mfaEnabled: Boolean,
    @SerializedName("mfa_key") val mfaKey: String?,
    @SerializedName("name") val name: String
)