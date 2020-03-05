package io.simplelogin.android.utils.model


data class UserLogin(
    val apiKey: String?,
    val mfaEnabled: Boolean,
    val mfaKey: String?,
    val name: String
)