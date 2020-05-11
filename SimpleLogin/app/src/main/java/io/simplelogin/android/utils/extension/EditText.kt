package io.simplelogin.android.utils.extension

import android.widget.EditText

fun EditText.placeCursorToEnd() = setSelection(text.length)