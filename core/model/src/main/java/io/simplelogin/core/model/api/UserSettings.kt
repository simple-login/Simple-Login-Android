package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName

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

    val value: String
        get() = when (this) {
            WORD -> "word"
            RANDOM_STRING -> "random_string"
        }
}