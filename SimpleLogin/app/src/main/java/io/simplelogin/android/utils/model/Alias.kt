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
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("enabled") private var _enabled: Boolean,
    @SerializedName("note") val note: String?,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int
): Parcelable {
    val enabled: Boolean
        get() = _enabled

    fun setEnabled(enabled: Boolean) {
        _enabled = enabled
    }

    private var _countSpannableString: Spannable? = null
    fun getCountSpannableString(context: Context): Spannable {
        if (_countSpannableString == null) {
            val darkGrayColor = ContextCompat.getColor(context, R.color.colorDarkGray)
            val blackColor = ContextCompat.getColor(context, android.R.color.black)
            val spannableString = SpannableStringBuilder()
                .color(blackColor) { append(" $forwardCount ") }
                .color(darkGrayColor) { append(if (forwardCount > 1) "forwards," else "forwards,") }
                .color(blackColor) { append(" $blockCount ") }
                .color(darkGrayColor) { append(if (blockCount > 1) "blocks," else "blocks,") }
                .color(blackColor) { append(" $replyCount ") }
                .color(darkGrayColor) { append(if (replyCount > 1) "replies," else "reply") }

            _countSpannableString = spannableString
        }

        return _countSpannableString!!
    }

    private var _creationString: String? = null
    fun getCreationString(): String {
        if (_creationString == null) {
            val distance = SLDateTimeFormatter.distanceFromNow(creationTimestamp)
            _creationString = "Created ${distance.first} ${distance.second} ago"
        }

        return _creationString!!
    }
}

data class AliasArray(
    @SerializedName("aliases") val aliases: List<Alias>
)