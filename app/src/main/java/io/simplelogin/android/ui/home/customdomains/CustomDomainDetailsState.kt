package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.models.api.ApiError

data class CustomDomainDetailsState(
    val domain: CustomDomain,
    val isUpdating: Boolean,
    val updateError: ApiError?,
    val isUpdated: Boolean
)