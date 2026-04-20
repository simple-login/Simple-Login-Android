package io.simplelogin.android.ui.home.aliascontacts

import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.Contact

data class AliasContactsState(
    val contacts: List<Contact> = emptyList(),
    val page: Int = 0,
    val isRefreshing: Boolean = true,
    val canLoadMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ApiError? = null
) {
    companion object {
        val Default = AliasContactsState()
    }
}
