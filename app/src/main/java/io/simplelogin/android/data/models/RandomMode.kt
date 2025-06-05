package io.simplelogin.android.data.models

import com.google.gson.annotations.SerializedName

enum class RandomMode {
    @SerializedName("uuid")
    UUID,
    @SerializedName("word")
    WORD
}
