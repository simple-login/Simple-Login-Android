package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

data class Mailbox(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("default") val default: Boolean,
    @SerializedName("creation_timestamp") val creationTimestamp: Double,
    @SerializedName("nb_alias") val aliasCount: Int,
    @SerializedName("verified") val verified: Boolean
)

data class Mailboxes(
    @SerializedName("mailboxes") val value: List<Mailbox>
)

@Serializable
data class MailboxLite(
    val id: Int,
    val email: String,
)