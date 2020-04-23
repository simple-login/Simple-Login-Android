package io.simplelogin.android.utils.extension

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Point
import android.net.Uri
import android.util.DisplayMetrics
import android.view.inputmethod.InputMethodManager

fun Activity.showKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.showSoftInput(this.currentFocus, InputMethodManager.SHOW_IMPLICIT)
}

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

fun Activity.startSendEmailIntent(emailAddress: String) {
    val intent = Intent(Intent.ACTION_SENDTO)
    intent.data = Uri.parse("mailto:")
    intent.putExtra(Intent.EXTRA_EMAIL, arrayOf(emailAddress))
    startActivity(intent)
}

fun Activity.getScreenHeight() : Int {
    val size = Point()
    windowManager.defaultDisplay.getRealSize(size)
    // y is height, x is width
    return size.y
}

fun Activity.openUrlInBrowser(url: String) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
    startActivity(intent)
}