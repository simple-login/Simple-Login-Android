package io.simplelogin.android.utils.extension

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import io.simplelogin.android.utils.enums.SLError

fun Fragment.toastError(error: SLError) {
    Toast.makeText(context, error.description, Toast.LENGTH_SHORT).show()
}

fun Fragment.copyToClipboard(label: String, text: String) : Boolean {
    return (activity as? AppCompatActivity)?.copyToClipboard(label, text) ?: false
}

fun Fragment.toastShortly(text: String) =
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()