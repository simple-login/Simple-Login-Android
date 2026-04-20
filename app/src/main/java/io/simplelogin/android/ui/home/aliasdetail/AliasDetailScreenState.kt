package io.simplelogin.android.ui.home.aliasdetail

import io.simplelogin.core.model.api.Alias
import io.simplelogin.core.model.api.AliasActivity
import io.simplelogin.core.model.api.ApiError

sealed class AliasDetailScreenState(open val alias: Alias?) {
    data object Loading : AliasDetailScreenState(null)
    data class Loaded(
        override val alias: Alias,
        val activities: List<AliasActivity>,
        val hasMoreActivities: Boolean
    ) : AliasDetailScreenState(alias)

    data class Error(val error: ApiError) : AliasDetailScreenState(null)
}