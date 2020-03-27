package io.simplelogin.android.utils.extension

fun String.isValidEmail() : Boolean {
    if (isEmpty()) return false
    val emailRegex = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    return emailRegex.matches(this)
}

fun String.isValidEmailPrefix() : Boolean {
    if (isEmpty()) return false

    val count = count()
    if (count > 100 || count == 0) return false

    val prefixRegex = Regex("""([0-9|A-Z|a-z|\-|_]*)""")
    return prefixRegex.matches(this)
}