package io.simplelogin.android.utils.extension

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.DisplayMetrics
import android.widget.Toast
import androidx.core.content.ContextCompat
import io.simplelogin.android.utils.enums.SLError

fun Context.toastShortly(text: String): Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}

fun Context.toastLongly(text: String): Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
    toast.show()
    return toast
}

fun Context.toastThrowable(throwable: Throwable) {
    toastShortly(throwable.localizedMessage ?: "Unknown error")
}

fun Context.toastError(error: SLError) = toastLongly(error.description)

fun Context.toastUpToDate() = toastShortly("You are up to date")

fun Context.getVersionName(): String {
    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    return packageInfo.versionName
}

fun Context.dpToPixel(dp: Float): Float =
        dp * (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

fun Context.pixelsToDp(px: Float): Float =
        px / (resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)

fun Context.canReadContacts(): Boolean =
        ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED