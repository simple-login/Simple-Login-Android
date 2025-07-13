package io.simplelogin.android.data.models.preferences

import io.simplelogin.android.R
import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default
) {
    companion object {
        val Default = DevicePreferences()
    }
}

enum class AliasCellSelection: OptionUiModel {
    VIEW_DETAILS, COPY_EMAIL;

    override fun titleResId() = when (this) {
        VIEW_DETAILS -> R.string.view_details
        COPY_EMAIL -> R.string.copy_alias_address
    }

    companion object {
        val Default = COPY_EMAIL
    }
}