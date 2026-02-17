package io.simplelogin.android.data.models.api

import android.content.Context
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.R

data class UserSettings(
    @SerializedName("alias_generator") val randomMode: RandomMode,
    @SerializedName("notification") val notification: Boolean,
    @SerializedName("random_alias_default_domain") val randomAliasDefaultDomain: String,
    @SerializedName("sender_format") val senderFormat: SenderFormat,
    @SerializedName("random_alias_suffix") val randomAliasSuffix: RandomAliasSuffix
)

enum class SenderFormat {
    @SerializedName("A")
    A,

    @SerializedName("AT")
    AT,

    @SerializedName("NAME_ONLY")
    NAME_ONLY,

    @SerializedName("AT_ONLY")
    AT_ONLY,

    @SerializedName("NO_NAME")
    NO_NAME
}

enum class RandomAliasSuffix {
    @SerializedName("word")
    WORD,

    @SerializedName("random_string")
    RANDOM_STRING;

    fun title(context: Context): String =
        when (this) {
            WORD -> context.getString(R.string.random_word)
            RANDOM_STRING -> context.getString(R.string.random_5_characters)
        }
}