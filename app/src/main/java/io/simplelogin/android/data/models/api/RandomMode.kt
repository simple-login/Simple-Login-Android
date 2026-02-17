package io.simplelogin.android.data.models.api

import android.content.Context
import com.google.gson.annotations.SerializedName
import io.simplelogin.android.R
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay.BOTTOM_SHEET
import io.simplelogin.android.data.models.preferences.AliasOptionsDisplay.DROPDOWN_MENU

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
