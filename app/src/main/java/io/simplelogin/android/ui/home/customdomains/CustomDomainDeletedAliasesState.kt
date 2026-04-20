package io.simplelogin.android.ui.home.customdomains

import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.DeletedAlias

data class CustomDomainDeletedAliasesState(
    val aliases: List<DeletedAlias>? = null,
    val isFetching: Boolean = true,
    val fetchError: ApiError? = null
) {
    companion object {
        val Default = CustomDomainDeletedAliasesState()
    }
}