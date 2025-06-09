package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

enum class RandomMode {
    @SerializedName("uuid")
    UUID,
    @SerializedName("word")
    WORD
}
