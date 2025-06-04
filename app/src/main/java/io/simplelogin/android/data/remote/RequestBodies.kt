package io.simplelogin.android.data.remote

import com.google.gson.annotations.SerializedName

data class ActivateAccountBody(
    val email: String,
    val code: String
)

data class EmailBody(
    val email: String
)

data class LoginBody(
    val email: String,
    val password: String,
    val device: String
)

data class MfaAuthBody(
    @SerializedName("mfa_token") val token: String,
    @SerializedName("mfa_key") val key: String,
    @SerializedName("device") val device: String
)

data class RegisterBody(
    val email: String,
    val password: String
)

data class UpdateProfilePictureBody(
    @SerializedName("profile_picture") val value: String
)

data class UpdateNameBody(
    val name: String
)