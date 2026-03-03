package io.simplelogin.android.ui.home.aliasactivities

import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.ApiError

data class AliasActivitiesState(
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