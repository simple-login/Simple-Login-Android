package io.simplelogin.android.data.models.preferences

import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.COPY_EMAIL
)

enum class AliasCellSelection {
    VIEW_DETAILS, COPY_EMAIL
}