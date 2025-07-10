package io.simplelogin.android.util

import android.util.Patterns

fun String.isValidEmail() = Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isValidPassword() = count() >= 8

fun String.isValidUrl() = Patterns.WEB_URL.matcher(this).matches()