package io.simplelogin.core.common

import android.util.Patterns

fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword() = count() >= 8

fun String.isValidUrl() = Patterns.WEB_URL.matcher(this).matches()

const val MAX_PREFIX_LENGTH = 40

enum class InvalidPrefixReason {
    TWO_CONSECUTIVE_DOTS,
    INVALID_CHARACTER,
    DOT_AT_THE_BEGINNING,
    DOT_AT_THE_END,
    PREFIX_TOO_LONG,
    PREFIX_EMPTY
}

sealed class PrefixValidationResult {
    data object Valid : PrefixValidationResult()
    data class Invalid(val reason: InvalidPrefixReason) : PrefixValidationResult()

    val isInvalid: Boolean get() = this is Invalid
}

fun String.validatePrefix(): PrefixValidationResult {
    if (isEmpty()) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.PREFIX_EMPTY)
    }

    if (length >= MAX_PREFIX_LENGTH) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.PREFIX_TOO_LONG)
    }

    if (contains("..")) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.TWO_CONSECUTIVE_DOTS)
    }

    if (startsWith(".")) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.DOT_AT_THE_BEGINNING)
    }

    if (endsWith(".")) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.DOT_AT_THE_END)
    }

    val validCharacters = ('a'..'z') + ('0'..'9') + '_' + '-' + '.'

    if (!all { validCharacters.contains(it) }) {
        return PrefixValidationResult.Invalid(InvalidPrefixReason.INVALID_CHARACTER)
    }

    return PrefixValidationResult.Valid
}