package io.simplelogin.android.ui.home.aliasdetail

import io.simplelogin.android.data.models.api.Alias
import io.simplelogin.android.data.models.api.AliasActivity
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.Mailbox
import io.simplelogin.android.data.models.preferences.DevicePreferences

data class AliasDetailScreenState(
    val alias: Alias?,
    val devicePreferences: DevicePreferences,
    val activitiesState: AliasActivitiesState,
    val mailboxesToUpdate: List<Mailbox>?
) {
    companion object {
        val Default = AliasDetailScreenState(
            alias = null,
            devicePreferences = DevicePreferences.Default,
            activitiesState = AliasActivitiesState.Loading,
            mailboxesToUpdate = null
        )
    }
}

sealed class AliasActivitiesState {
    object Loading : AliasActivitiesState()
    data class Loaded(val activities: List<AliasActivity>) : AliasActivitiesState()
    data class Error(val error: ApiError) : AliasActivitiesState()
}
