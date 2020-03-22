package io.simplelogin.android.utils.extension

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputMethodManager

fun Activity.dismissKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus?.windowToken, 0)
    }
}

fun Activity.copyToClipboard(label: String, text: String) : Boolean {
    val clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) ?: false) as ClipboardManager
    clipboardManager.primaryClip = ClipData.newPlainText(label, text)
    return true
}