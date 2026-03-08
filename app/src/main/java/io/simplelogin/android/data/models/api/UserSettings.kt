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
    NO_NAME;

    fun description(context: Context): String =
        when (this) {
            A -> "John Doe - john.doe(a)example.com"
            AT -> "John Doe - john.doe at example.com"
            NAME_ONLY -> "John Doe"
            AT_ONLY -> "john at example.com"
            NO_NAME -> context.getString(R.string.no_name_format_description)
        }

    val value: String
        get() = when (this) {
            A -> "A"
            AT -> "AT"
            NAME_ONLY -> "NAME_ONLY"
            AT_ONLY -> "AT_ONLY"
            NO_NAME -> "NO_NAME"
        }
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

    val value: String
        get() = when (this) {
            WORD -> "word"
            RANDOM_STRING -> "random_string"
        }
}