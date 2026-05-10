package io.simplelogin.feature.aliasactivities

import io.simplelogin.core.model.api.AliasActivity
import io.simplelogin.core.model.api.ApiError

internal data class AliasActivitiesState(
    val activities: List<AliasActivity> = emptyList(),
    val page: Int = 0,
    val isRefreshing: Boolean = true,
    val canLoadMore: Boolean = false,
    val isLoadingMore: Boolean = false,
    val error: ApiError? = null
) {
    companion object {
        val Default = AliasActivitiesState()
    }
}
