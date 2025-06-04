package io.simplelogin.android.data.remote.request_body

data class LoginBody(
    val email: String,
    val password: String,
    val device: String
)
