package io.simplelogin.core.common

import io.simplelogin.core.model.api.AliasActivity
import io.simplelogin.core.model.ui.ActivityUiAction

interface ActivityUiActionHandler {
    suspend fun handleActivityAction(activity: AliasActivity, action: ActivityUiAction)
}