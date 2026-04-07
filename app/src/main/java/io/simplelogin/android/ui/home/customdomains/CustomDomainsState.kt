package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.android.data.models.api.CustomDomain
import io.simplelogin.android.models.api.ApiError

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