package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.android.core.model.api.ApiError
import io.simplelogin.android.core.model.api.CustomDomain

data class CustomDomainsState(
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