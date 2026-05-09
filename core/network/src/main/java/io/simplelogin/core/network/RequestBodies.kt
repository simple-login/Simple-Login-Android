package io.simplelogin.core.network

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

data class CreateAliasBody(
    @SerializedName("alias_prefix") val prefix: String,
    @SerializedName("signed_suffix") val signedSuffix: String,
    @SerializedName("mailbox_ids") val mailboxIds: List<Int>,
    @SerializedName("note") val note: String?,
    @SerializedName("name") val name: String?
)

data class CreateContactBody(
    val contact: String
)

data class SearchBody(
    val query: String
)

data class NoteBody(
    @SerializedName("note") val note: String?
)

data class PasswordBody(
    val password: String
)

data class TransferAliasesBody(
    @SerializedName("transfer_aliases_to") val mailboxId: Int = -1 // -1 is equal to passing nothing
)
