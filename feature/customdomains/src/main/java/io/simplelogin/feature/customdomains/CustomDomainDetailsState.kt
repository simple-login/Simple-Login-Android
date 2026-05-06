package io.simplelogin.feature.customdomains

import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.CustomDomain

internal data class CustomDomainDetailsState(
    val domain: CustomDomain,
    val isUpdating: Boolean,
    val updateError: ApiError?,
    val isUpdated: Boolean
)