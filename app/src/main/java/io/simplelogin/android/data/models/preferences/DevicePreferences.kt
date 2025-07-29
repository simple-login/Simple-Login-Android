package io.simplelogin.android.data.models.preferences

import android.content.Context
import io.simplelogin.android.R
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
    CREATION_DATE, LATEST_ACTIVITY, NOTE, MAILBOXES, LAST_14_DAYS;

    fun title(context: Context) = when (this) {
        CREATION_DATE -> context.getString(R.string.creation_date)
        LATEST_ACTIVITY -> context.getString(R.string.latest_activity)
        NOTE -> context.getString(R.string.note)
        MAILBOXES -> context.getString(R.string.mailboxes)
        LAST_14_DAYS -> context.getString(R.string.last_14_days)
    }
}