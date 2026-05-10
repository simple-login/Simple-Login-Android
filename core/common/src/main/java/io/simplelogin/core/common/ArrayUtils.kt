package io.simplelogin.core.common

inline fun <reified T> Array<Any?>.getAs(index: Int): T? =
    getOrNull(index)?.let { value ->
        value as? T
    }

fun Array<Any?>.getAs(index: Int, default: Boolean): Boolean =
    getOrNull(index)?.let { value ->
        value as? Boolean ?: default
    } ?: default
