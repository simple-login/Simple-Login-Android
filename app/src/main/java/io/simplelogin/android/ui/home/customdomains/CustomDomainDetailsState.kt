package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.CustomDomain

data class CustomDomainDetailsState(
    val domain: CustomDomain,
    val isUpdating: Boolean,
    val updateError: ApiError?,
    val isUpdated: Boolean
)