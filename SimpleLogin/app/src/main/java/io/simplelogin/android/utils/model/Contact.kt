package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.utils.SLDateTimeFormatter
import io.simplelogin.android.utils.interfaces.Reversable

data class Contact(
    @SerializedName("id") val id: Int,
    @SerializedName("contact") override val email: String,
    @SerializedName("reverse_alias") override val reverseAlias: String,
    @SerializedName("reverse_alias_address") override val reverseAliasAddress: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("last_email_sent_date") val lastEmailSentDate: String?,
    @SerializedName("last_email_sent_timestamp") val lastEmailSentTimestamp: Long?,
    @SerializedName("existed") val existed: Boolean,
    @SerializedName("block_forward") var blockForward: Boolean
) : Reversable {
    private var _creationString: String? = null
    fun getCreationString(): String {
        if (_creationString == null) {
            _creationString = SLDateTimeFormatter.preciseCreationDateStringFrom(creationTimestamp, "Created on")
        }

        return _creationString!!
    }

    private var _lastEmailSentString: String? = null
    fun getLastEmailSentString(): String? {
        if (lastEmailSentTimestamp != null) {
            if (_lastEmailSentString == null) {
                _lastEmailSentString =
                    SLDateTimeFormatter.preciseCreationDateStringFrom(lastEmailSentTimestamp, "Last sent on")
            }

            return _lastEmailSentString!!
        }

        return null
    }
}

data class ContactArray(
    @SerializedName("contacts") val contacts: List<Contact>
)

data class ContactToggleResult(
    @SerializedName("block_forward") val blockForward: Boolean
)