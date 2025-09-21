package io.simplelogin.android.ui.home.aliasdetail

import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.ApiError

data class AliasDetailScreenState(
    val activitiesState: AliasActivitiesState
) {
    companion object {
        val Default = AliasDetailScreenState(
            activitiesState = AliasActivitiesState.Loading
        )
    }
}

sealed class AliasActivitiesState {
    object Loading : AliasActivitiesState()
    data class Loaded(val activities: List<AliasActivity>) : AliasActivitiesState()
    data class Error( val error: ApiError) : AliasActivitiesState()
}
