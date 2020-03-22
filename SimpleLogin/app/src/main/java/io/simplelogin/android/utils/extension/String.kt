package io.simplelogin.android.utils.extension

fun String.isValidEmail() : Boolean {
    val emailRegex = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    if (isEmpty()) return false
    return emailRegex.matches(this)
}