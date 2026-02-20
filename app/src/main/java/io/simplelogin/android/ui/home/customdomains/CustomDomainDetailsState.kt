package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.CustomDomain

data class CustomDomainDetailsState(
    val domain: CustomDomain,
    val isUpdating: Boolean,
    val updateError: ApiError?
)