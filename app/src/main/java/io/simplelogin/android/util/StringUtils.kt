package io.simplelogin.android.util

import android.util.Patterns

fun String.validEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.validPassword() = count() >= 8