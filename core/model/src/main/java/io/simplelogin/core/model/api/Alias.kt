package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@[JvmInline Serializable]
value class AliasId(val value: Int)

@Serializable
data class Alias(
    @SerializedName("id") val id: AliasId,
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
    @Serializable
    data class LatestActivity(
        val action: ActivityAction,
        val contact: Contact,
        val timestamp: Double
    ) {
        @Serializable
        data class Contact(
            @SerializedName("email") val email: String,
            @SerializedName("name") val name: String?,
            @SerializedName("reverse_alias") val reverseAlias: String
        )
    }

    val hasActivities: Boolean
        get() = (forwardCount + replyCount + blockCount) > 0

    val mailtoEmail: String
        get() = "mailto:$email"

    val displayedEmail: String
        get() {
            // Add 🚫 prefix to disabled alias
            val email = if (enabled) email else "\uD83D\uDEAB $email"
            // Break just before "@" to make multi-lines emails more readable
            return email.replace("@", "\u200B@")
        }

    companion object
}

data class Aliases(
    @SerializedName("aliases") val aliases: List<Alias>
)
