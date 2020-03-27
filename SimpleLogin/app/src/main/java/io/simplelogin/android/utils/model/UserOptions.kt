package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName

data class UserOptions(
    @SerializedName("can_create") val canCreate: Boolean,
    @SerializedName("suffixes") val suffixes: List<String>
)