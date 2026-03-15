package io.simplelogin.android.data.models.ui

import io.simplelogin.android.data.models.api.ApiKey

data class DialogPayload(val apiKey: ApiKey)
data class ObjectDialogPayload<T>(val apiKey: ApiKey, val value: T)