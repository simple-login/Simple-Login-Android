package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.utils.SLDateTimeFormatter
import io.simplelogin.android.utils.interfaces.Reversable
import kotlinx.android.parcel.IgnoredOnParcel

data class AliasActivity(
    @SerializedName("action") val action: Action,
    @SerializedName("from") val from: String,
    @SerializedName("to") val to: String,
    @SerializedName("timestamp") val timestamp: Long,
    @SerializedName("reverse_alias") override val reverseAlias: String,
    @SerializedName("reverse_alias_address") override val reverseAliasAddress: String
) : Reversable {
    @IgnoredOnParcel
    private var _timestampString: String? = null
    fun getTimestampString(): String {
        if (_timestampString == null) {
            _timestampString = SLDateTimeFormatter.preciseCreationDateStringFrom(timestamp)
        }

        return _timestampString!!
    }

    override val email: String
        get() = when (action) {
            Action.REPLY -> to
            else -> from
        }
}

enum class Action {
    @SerializedName("reply") REPLY,
    @SerializedName("block") BLOCK,
    @SerializedName("forward") FORWARD,
    @SerializedName("bounced") BOUNCED
}

data class AliasActivityArray(
    @SerializedName("activities") val activities: List<AliasActivity>
)
