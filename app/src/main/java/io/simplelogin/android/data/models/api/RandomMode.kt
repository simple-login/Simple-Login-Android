package io.simplelogin.android.data.models.api

import android.content.Context
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.R

enum class RandomMode {
    @SerializedName("uuid")
    UUID,

    @SerializedName("word")
    WORD;

    fun title(context: Context) = when (this) {
        UUID -> "UUID"
        WORD -> context.getString(R.string.random_word)
    }
}
