package io.simplelogin.android.data.models.api

import android.content.Context
import android.text.format.DateUtils
import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@[JvmInline Serializable]
value class AliasId(val value: Int)

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

        val relativeTime: String
            get() = timestamp.relativeTimeSpan()
    }

    val hasActivities: Boolean
        get() = (forwardCount + replyCount + blockCount) > 0

    val displayedEmail: String
        get() {
            // Add 🚫 prefix to disabled alias
            val email = if (enabled) email else "\uD83D\uDEAB $email"
            // Break just before "@" to make multi-lines emails more readable
            return email.replace("@", "\u200B@")
        }

    fun relativeCreationTime(context: Context) = creationTimestamp.relativeDateTime(context)

    companion object
}

data class Aliases(
    @SerializedName("aliases") val aliases: List<Alias>
)

private fun Double.relativeDateTime(context: Context): String =
    DateUtils.getRelativeDateTimeString(
        context,
        (this * 1_000).toLong(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_TIME or
                DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_ABBREV_RELATIVE or
                DateUtils.FORMAT_ABBREV_MONTH
    ).toString()

private fun Double.relativeTimeSpan(): String =
    DateUtils.getRelativeTimeSpanString(
        (this * 1_000).toLong(),
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()