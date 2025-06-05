package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class Alias(
    @SerialName("id") val id: Int,
    @SerialName("email") val email: String,
    @SerialName("name") val name: String?,
    @SerialName("enabled") val enabled: Boolean,
    @SerialName("creation_timestamp") val creationTimestamp: Double,
    @SerialName("nb_block") val blockCount: Int,
    @SerialName("nb_forward") val forwardCount: Int,
    @SerialName("nb_reply") val replyCount: Int,
    @SerialName("note") val note: String?,
    @SerialName("support_pgp") val pgpSupported: Boolean,
    @SerialName("disable_pgp") val pgpDisabled: Boolean,
    @SerialName("mailboxes") val mailboxes: List<Mailbox>,
    @SerialName("latest_activity") val latestActivity: LatestActivity?,
    @SerialName("pinned") val pinned: Boolean
) {
    data class Mailbox(
        val id: Int,
        val email: String,
    )

    data class LatestActivity(
        val action: ActivityAction,
        val contact: Contact,
        val timestamp: Double
    ) {
        data class Contact(
            @SerialName("email") val email: String,
            @SerialName("name") val name: String?,
            @SerialName("reverse_alias") val reverseAlias: String
        )
    }
}

data class Aliases(
    @SerialName("aliases") val aliases: List<Alias>
)