package io.simplelogin.android.utils.extension

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import io.simplelogin.android.utils.enums.SLError

fun AppCompatActivity.dismissKeyboard() {
    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    if (inputMethodManager.isAcceptingText) {
        inputMethodManager.hideSoftInputFromWindow(this.currentFocus.windowToken, 0)
    }
}

fun AppCompatActivity.toastError(error: SLError) {
    toastShortly(error.description)
}

fun AppCompatActivity.copyToClipboard(label: String, text: String) : Boolean {
    val clipboardManager = (getSystemService(Context.CLIPBOARD_SERVICE) ?: false) as ClipboardManager
    clipboardManager.primaryClip = ClipData.newPlainText(label, text)
    return true
}

fun AppCompatActivity.toastShortly(text: String) =
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()