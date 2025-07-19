package io.simplelogin.android.data.models.ui

import android.content.Context
import io.simplelogin.android.R

enum class AliasFilterMode {
    ALL, ENABLED, DISABLED;

    fun title(context: Context) =
        when (this) {
            ALL -> context.getString(R.string.all_aliases)
            ENABLED -> context.getString(R.string.active_aliases)
            DISABLED -> context.getString(R.string.disabled_aliases)
        }
}