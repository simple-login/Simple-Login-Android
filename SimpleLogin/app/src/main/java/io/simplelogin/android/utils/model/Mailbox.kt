package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.utils.SLDateTimeFormatter

data class Mailbox (
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("default") val isDefault: Boolean,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("nb_alias") val aliasCount: Int,
    @SerializedName("verified") val isVerified: Boolean
) {
    private var _creationString: String? = null
    fun getCreationString(): String {
        if (_creationString == null) {
            val distance = SLDateTimeFormatter.distanceFromNow(creationTimestamp)
            _creationString = "Created ${distance.first} ${distance.second} ago"
        }

        return _creationString!!
    }

    private var _aliasCountString: String? = null
    fun getAliasCountString(): String {
        if (_aliasCountString == null) {
            _aliasCountString = "$aliasCount aliases"
        }

        return _aliasCountString!!
    }

    fun toAliasMailbox() = AliasMailbox(id, email)
}

data class MailboxArray(
    @SerializedName("mailboxes") val mailboxes: List<Mailbox>
)
