package io.simplelogin.android.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.copyToClipboard(label: String, text: String) : Boolean {
    return (activity as? AppCompatActivity)?.copyToClipboard(label, text) ?: false
}
