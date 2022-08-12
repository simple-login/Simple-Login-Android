package io.simplelogin.android.utils.extension

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

fun Fragment.copyToClipboard(label: String, text: String) : Boolean =
    (activity as? AppCompatActivity)?.copyToClipboard(label, text) ?: false

fun Fragment?.runOnUiThread(action: () -> Unit) {
    this ?: return
    if (!isAdded) return // Fragment not attached to an Activity
    activity?.runOnUiThread(action)
}