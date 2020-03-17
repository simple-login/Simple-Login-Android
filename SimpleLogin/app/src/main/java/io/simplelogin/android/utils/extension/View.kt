package io.simplelogin.android.utils.extension

import android.view.View

fun View.customSetEnabled(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1.0f else 0.5f
}