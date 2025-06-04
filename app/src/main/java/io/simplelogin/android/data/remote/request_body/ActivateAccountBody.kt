package io.simplelogin.android.data.remote.request_body

data class ActivateAccountBody(
    val email: String,
    val code: String
)