package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.CustomDomain

data class CustomDomainDetailsState(
    val domain: CustomDomain,
    val isUpdating: Boolean,
    val updateError: ApiError?,
    val isUpdated: Boolean
)