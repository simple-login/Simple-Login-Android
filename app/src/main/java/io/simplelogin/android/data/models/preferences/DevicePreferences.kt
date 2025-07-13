package io.simplelogin.android.data.models.preferences

import io.simplelogin.android.R
import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default,
    val swipeFromLeftToRightAction: SwipeAction = SwipeAction.DISABLE_ENABLE,
    val swipeFromRightToLeftAction: SwipeAction = SwipeAction.PIN_UNPIN
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

enum class SwipeAction: OptionUiModel {
    DISABLE_ENABLE, PIN_UNPIN, DELETE;

    override fun titleResId() = when (this) {
        DISABLE_ENABLE -> R.string.disable_enable
        PIN_UNPIN -> R.string.pin_unpin
        DELETE -> R.string.delete
    }
}