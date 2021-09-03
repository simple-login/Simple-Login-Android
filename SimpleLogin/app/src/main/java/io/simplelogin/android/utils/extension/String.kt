package io.simplelogin.android.utils.extension

fun String.isValidEmail() : Boolean {
    if (isEmpty()) return false
    val emailRegex = Regex("[A-Z0-9a-z._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,64}")
    return emailRegex.matches(this)
}

@Suppress("MagicNumber")
fun String.isValidEmailPrefix() : Boolean {
    if (isEmpty() || length > 100) return false

    val prefixRegex = Regex("""([0-9|A-Z|a-z|\-|_|\.]*)""")
    return prefixRegex.matches(this)
}

@Suppress("LoopWithTooManyJumpStatements")
fun String.extractWebsiteName() : String {
    var websiteName = ""
    var reachedFirstDot = false
    for (char in reversed()) {
        if (!reachedFirstDot && char != '.') continue

        if (char == '.') {
            if (!reachedFirstDot) {
                reachedFirstDot = true
                continue
            }
            break
        }
        if (char == '/') break
        websiteName += char
    }

    return websiteName.reversed()
}

fun String.extractFirstWord() =
    when {
        contains(' ') -> substring(0, indexOf(' '))
        else -> this
    }
