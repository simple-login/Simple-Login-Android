package io.simplelogin.android.util

import android.content.Context
import io.simplelogin.android.R
import io.simplelogin.android.models.api.ApiError

fun ApiError.description(context: Context): String = when (this) {
    is ApiError.HttpError -> {
        if (errorMessage != null) {
            return errorMessage + " (${code})"
        }
        when (this.code) {
            429 -> context.getString(R.string.too_many_requests)
            in 400..499 -> context.getString(R.string.client_error, this.code)
            in 500..599 -> context.getString(R.string.internal_server_error, this.code)
            else -> context.getString(R.string.unknown_http_error, this.code)
        }
    }

    is ApiError.UnknownError -> context.getString(R.string.generic_error, e.toString())
}