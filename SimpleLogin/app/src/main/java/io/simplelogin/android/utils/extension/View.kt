package io.simplelogin.android.utils.extension

import android.view.View
import android.view.ViewOutlineProvider

fun View.customSetEnabled(enabled: Boolean) {
    isEnabled = enabled
    alpha = if (enabled) 1.0f else 0.5f
}

fun View.makeSubviewsClippedToBound() {
    outlineProvider = ViewOutlineProvider.BACKGROUND
    clipToOutline = true
}