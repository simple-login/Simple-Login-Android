package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("creation_timestamp") val creationTimestamp: Double,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int,
    @SerializedName("note") val note: String?,
    @SerializedName("support_pgp") val pgpSupported: Boolean,
    @SerializedName("disable_pgp") val pgpDisabled: Boolean,
    @SerializedName("mailboxes") val mailboxes: List<MailboxLite>,
    @SerializedName("latest_activity") val latestActivity: LatestActivity?,
    @SerializedName("pinned") val pinned: Boolean
) {
    data class LatestActivity(
        val action: ActivityAction,
        val contact: Contact,
        val timestamp: Double
    ) {
        data class Contact(
            @SerializedName("email") val email: String,
            @SerializedName("name") val name: String?,
            @SerializedName("reverse_alias") val reverseAlias: String
        )
    }
}

data class Aliases(
    @SerializedName("aliases") val aliases: List<Alias>
)