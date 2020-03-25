package io.simplelogin.android.utils.extension

import android.content.Context
import android.content.pm.PackageManager
import android.widget.Toast
import io.simplelogin.android.utils.enums.SLError

fun Context.toastShortly(text: String) : Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_SHORT)
    toast.show()
    return toast
}


fun Context.toastLongly(text: String) : Toast {
    val toast = Toast.makeText(this, text, Toast.LENGTH_LONG)
    toast.show()
    return toast
}

fun Context.toastError(error: SLError) =
    toastShortly(error.description)

fun Context.toastUpToDate() =
    toastShortly("You are up to date")

fun Context.getVersionName() : String {
    val packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
    return packageInfo.versionName
}