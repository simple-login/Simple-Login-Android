package io.simplelogin.android.utils.extension

import android.widget.TextView
import androidx.annotation.DrawableRes

fun TextView.setDrawableStart(@DrawableRes id: Int = 0) {
    setCompoundDrawablesRelativeWithIntrinsicBounds(id, 0, 0, 0)
}
