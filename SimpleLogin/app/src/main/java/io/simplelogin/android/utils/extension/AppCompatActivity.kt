package io.simplelogin.android.utils.extension

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
    Toast.makeText(this, error.description, Toast.LENGTH_SHORT).show()
}

fun AppCompatActivity.toastApiKeyIsNull() {
    Toast.makeText(this, "API key is null", Toast.LENGTH_SHORT).show()
}