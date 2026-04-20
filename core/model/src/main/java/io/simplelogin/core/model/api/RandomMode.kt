package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName

enum class RandomMode {
    @SerializedName("uuid")
    UUID,

    @SerializedName("word")
    WORD;

    val value: String
        get() = when (this) {
            UUID -> "uuid"
            WORD -> "word"
        }
}