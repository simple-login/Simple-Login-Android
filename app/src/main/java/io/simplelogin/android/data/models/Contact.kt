package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

typealias ContactID = Int

data class Contact(
    @SerialName("id") val id: ContactID,
    @SerialName("contact") val email: String,
    @SerialName("creation_timestamp") val creationTimestamp: Double,
    @SerialName("last_email_sent_timestamp") val lastEmailSentTimestamp: Double?,
    @SerialName("reverse_alias") val reverseAlias: String,
    @SerialName("reverse_alias_address") val reverseAliasAddress: String,
    // `true` when the contact already exists but is asked to be created again
    @SerialName("existed") val existed: Boolean = false,
    @SerialName("block_forward") val blockForward: Boolean
)

data class Contacts(
    val contacts: List<Contact>
)