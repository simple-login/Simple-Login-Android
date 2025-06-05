package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class Mailbox(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("default") val default: Boolean,
    @SerialName("creation_timestamp") val creationTimestamp: Double,
    @SerialName("nb_alias") val aliasCount: Int,
    @SerialName("verified") val verified: Boolean
)

data class Mailboxes(
    @SerialName("mailboxes") val value: List<Mailbox>
)

data class MailboxLite(
    val id: Int,
    val email: String,
)