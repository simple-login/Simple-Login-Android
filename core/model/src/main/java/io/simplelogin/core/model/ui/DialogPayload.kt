package io.simplelogin.core.model.ui

import io.simplelogin.core.model.api.ApiKey

data class DialogPayload(val apiKey: ApiKey)
data class ObjectDialogPayload<T>(val apiKey: ApiKey, val value: T)