package io.simplelogin.android.data.models.preferences

import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default,
    val swipeFromLeftToRightAction: SwipeAction = SwipeAction.DISABLE_ENABLE,
    val swipeFromRightToLeftAction: SwipeAction = SwipeAction.PIN_UNPIN,
    val aliasDisplayInfos: List<AliasDisplayInfo> = AliasDisplayInfo.entries.toTypedArray().toList()
) {
    companion object {
        val Default = DevicePreferences()
    }
}

enum class AliasCellSelection {
    VIEW_DETAILS, COPY_EMAIL;

    companion object {
        val Default = COPY_EMAIL
    }
}

enum class SwipeAction {
    DISABLE_ENABLE, PIN_UNPIN, DELETE
}

enum class AliasDisplayInfo {
    CREATION_DATE, LATEST_ACTIVITY, NOTE, MAILBOXES, LAST_14_DAYS
}