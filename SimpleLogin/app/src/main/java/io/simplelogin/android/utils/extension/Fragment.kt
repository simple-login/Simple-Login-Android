package io.simplelogin.android.utils.extension

import android.widget.Toast
import androidx.fragment.app.Fragment
import io.simplelogin.android.utils.enums.SLError

fun Fragment.toastError(error: SLError) {
    Toast.makeText(context, error.description, Toast.LENGTH_SHORT).show()
}