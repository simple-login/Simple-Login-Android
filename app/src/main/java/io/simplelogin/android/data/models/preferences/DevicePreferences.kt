package io.simplelogin.android.data.models.preferences

import android.content.Context
import io.simplelogin.android.R
import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default,
    val aliasOptionsDisplay: AliasOptionsDisplay = AliasOptionsDisplay.Default,
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

    fun title(context: Context) = when (this) {
        VIEW_DETAILS -> context.getString(R.string.view_details)
        COPY_EMAIL -> context.getString(R.string.copy_alias_address)
    }
}

enum class AliasOptionsDisplay {
    BOTTOM_SHEET, DROPDOWN_MENU;

    companion object {
        val Default = BOTTOM_SHEET
    }

    fun title(context: Context) = when (this) {
        BOTTOM_SHEET -> context.getString(R.string.bottom_sheet)
        DROPDOWN_MENU -> context.getString(R.string.drop_down_menu)
    }
}

enum class SwipeAction {
    DISABLE_ENABLE, PIN_UNPIN, DELETE;

    fun title(context: Context) = when (this) {
        DISABLE_ENABLE -> context.getString(R.string.disable_enable)
        PIN_UNPIN -> context.getString(R.string.pin_unpin)
        DELETE -> context.getString(R.string.delete)
    }
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