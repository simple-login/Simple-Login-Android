package io.simplelogin.feature.customdomains

import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.CustomDomain

internal data class CustomDomainsState(
    val domains: List<CustomDomain>?,
    val isFetching: Boolean,
    val fetchError: ApiError?
) {
    companion object {
        val Default = CustomDomainsState(
            domains = null,
            isFetching = true,
            fetchError = null
        )
    }
}