package io.simplelogin.android.core.model.api

import com.google.gson.annotations.SerializedName

typealias ContactID = Int

data class Contact(
    @SerializedName("id") val id: ContactID,
    @SerializedName("contact") val email: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Double,
    @SerializedName("last_email_sent_timestamp") val lastEmailSentTimestamp: Double?,
    @SerializedName("reverse_alias") val reverseAlias: String,
    @SerializedName("reverse_alias_address") val reverseAliasAddress: String,
    // `true` when the contact already exists but is asked to be created again
    @SerializedName("existed") val existed: Boolean = false,
    @SerializedName("block_forward") val blockForward: Boolean
)

data class Contacts(
    val contacts: List<Contact>
)