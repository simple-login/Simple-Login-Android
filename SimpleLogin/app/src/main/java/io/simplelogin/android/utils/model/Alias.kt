package io.simplelogin.android.utils.model

import android.content.Context
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.text.color
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.R

data class Alias(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String,
    @SerializedName("creation_date") val creationDate: String,
    @SerializedName("creation_timestamp") val creationTimestamp: Long,
    @SerializedName("enabled") val enabled: Boolean,
    @SerializedName("note") val note: String?,
    @SerializedName("nb_block") val blockCount: Int,
    @SerializedName("nb_forward") val forwardCount: Int,
    @SerializedName("nb_reply") val replyCount: Int
) {
    private var _creationSpannableString: Spannable? = null
    fun getCreationSpannableString(context: Context): Spannable {
        if (_creationSpannableString == null) {
            val darkGrayColor = ContextCompat.getColor(context, R.color.colorDarkGray)
            val blackColor = ContextCompat.getColor(context, android.R.color.black)
            val spannableString = SpannableStringBuilder()
                .color(blackColor) { append(" $forwardCount ") }
                .color(darkGrayColor) { append(if (forwardCount > 1) "forwards," else "forwards,") }
                .color(blackColor) { append(" $blockCount ") }
                .color(darkGrayColor) { append(if (blockCount > 1) "blocks," else "blocks,") }
                .color(blackColor) { append(" $replyCount ") }
                .color(darkGrayColor) { append(if (replyCount > 1) "replies," else "reply") }

            _creationSpannableString = spannableString
        }

        return _creationSpannableString!!
    }
}

data class AliasArray(
    @SerializedName("aliases") val aliases: List<Alias>
)