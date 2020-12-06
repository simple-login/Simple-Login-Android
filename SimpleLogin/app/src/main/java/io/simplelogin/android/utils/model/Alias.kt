package io.simplelogin.android.utils.model

import android.content.Context
import android.os.Parcelable
import android.text.Spannable
import android.text.SpannableStringBuilder
import androidx.core.content.ContextCompat
import androidx.core.text.color
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.R
import io.simplelogin.android.utils.SLDateTimeFormatter
import kotlinx.android.parcel.IgnoredOnParcel
import kotlinx.android.parcel.Parcelize

@Suppress("ConstructorParameterNaming")
@Parcelize
data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("mailboxes") private var _mailboxes: List<AliasMailbox>,
    @SerializedName("enabled") private var _enabled: Boolean,
    @SerializedName("name") private var _name: String?,
    @SerializedName("note") private var _note: String?,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int,
    @SerializedName("latest_activity") val latestActivity: LatestActivity?
) : Parcelable {
    // Expose enabled
    val enabled: Boolean
        get() = _enabled

    fun setEnabled(enabled: Boolean) {
        _enabled = enabled
    }

    // Expose mailboxes
    val mailboxes: List<AliasMailbox>
        get() = _mailboxes

    fun setMailboxes(context: Context, mailboxes: List<AliasMailbox>) {
        _mailboxes = mailboxes
        generateMailboxesString(context)
    }

    // Expose name
    val name: String?
        get() = _name

    fun setName(name: String?) {
        _name = name
    }

    // Expose note
    val note: String?
        get() {
            if (_note == "") return null
            return _note
        }

    fun setNote(note: String?) {
        _note = note
    }

    val handleCount: Int
        get() = blockCount + forwardCount + replyCount

    @IgnoredOnParcel
    private var _countSpannableString: Spannable? = null
    fun getCountSpannableString(context: Context): Spannable {
        if (_countSpannableString == null) {
            val darkGrayColor = ContextCompat.getColor(context, R.color.colorDarkGray)
            val blackColor = ContextCompat.getColor(context, R.color.colorText)
            val spannableString = SpannableStringBuilder()
                .color(blackColor) { append("$forwardCount ") }
                .color(darkGrayColor) { append(if (forwardCount > 1) "forwards," else "forwards,") }
                .color(blackColor) { append(" $blockCount ") }
                .color(darkGrayColor) { append(if (blockCount > 1) "blocks," else "blocks,") }
                .color(blackColor) { append(" $replyCount ") }
                .color(darkGrayColor) { append(if (replyCount > 1) "replies," else "reply") }

            _countSpannableString = spannableString
        }

        return _countSpannableString!!
    }

    @IgnoredOnParcel
    private var _mailboxesString: Spannable? = null
    fun getMailboxesString(context: Context): Spannable {
        if (_mailboxesString == null) {
            generateMailboxesString(context)
        }

        return _mailboxesString!!
    }

    private fun generateMailboxesString(context: Context) {
        _mailboxesString = _mailboxes.sorted().toSpannableString(context)
    }

    @IgnoredOnParcel
    private var _creationString: String? = null
    fun getCreationString(): String {
        if (_creationString == null) {
            val distance = SLDateTimeFormatter.distanceFromNow(creationTimestamp)
            _creationString = "Created ${distance.first} ${distance.second} ago"
        }

        return _creationString!!
    }

    @IgnoredOnParcel
    private var _preciseCreationString: String? = null
    fun getPreciseCreationString(): String {
        if (_preciseCreationString == null) {
            _preciseCreationString =
                SLDateTimeFormatter.preciseCreationDateStringFrom(creationTimestamp, "Created on")
        }

        return _preciseCreationString!!
    }

    @IgnoredOnParcel
    private var _latestActivityString: String? = null
    fun getLatestActivityString(): String? =
        when (latestActivity) {
            null -> null
            else -> {
                if (_latestActivityString == null) {
                    val distance = SLDateTimeFormatter.distanceFromNow(latestActivity.timestamp)
                    _latestActivityString =
                        "${latestActivity.contact.email} • ${distance.first} ${distance.second} ago"
                }

                _latestActivityString!!
            }
        }
}

data class AliasArray(
    @SerializedName("aliases") val aliases: List<Alias>
)

fun List<AliasMailbox>.toSpannableString(context: Context): Spannable {
    val primaryColor = ContextCompat.getColor(context, R.color.colorPrimary)
    val blackColor = ContextCompat.getColor(context, R.color.colorText)
    val spannableString = SpannableStringBuilder()

    forEachIndexed { index, aliasMailbox ->
        spannableString.color(blackColor) { append(" ${aliasMailbox.email} ") }
        if (index != size - 1) {
            spannableString.color(primaryColor) { append("&") }
        }
    }

    return spannableString
}
