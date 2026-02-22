package io.simplelogin.android.data.models.preferences

import android.content.Context
import io.simplelogin.android.R
import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val theme: Theme = Theme.MATCH_SYSTEM,
    val showStats: Boolean = true,
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default,
    val aliasOptionsDisplay: AliasOptionsDisplay = AliasOptionsDisplay.Default,
    val swipeFromLeftToRightAction: SwipeAction = SwipeAction.DISABLE_ENABLE,
    val swipeFromRightToLeftAction: SwipeAction = SwipeAction.PIN_UNPIN,
    val aliasDisplayInfos: List<AliasDisplayInfo> = AliasDisplayInfo.entries.toTypedArray()
        .toList(),
    val defaultPrefix: DefaultPrefix = DefaultPrefix.RANDOM_WORD,
    val prefixRandomCharacterCount: Int = 5
) {
    companion object {
        val Default = DevicePreferences()
    }
}

enum class AliasCellSelection {
    VIEW_DETAILS, COPY_EMAIL, VIEW_OPTIONS;

    companion object {
        val Default = COPY_EMAIL
    }

    fun title(context: Context) = when (this) {
        VIEW_DETAILS -> context.getString(R.string.view_details)
        COPY_EMAIL -> context.getString(R.string.copy_alias_address)
        VIEW_OPTIONS -> context.getString(R.string.view_options)
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

enum class DefaultPrefix {
    EMPTY, RANDOM_WORD, RANDOM_CHARACTERS;

    fun title(context: Context) = when (this) {
        EMPTY -> context.getString(R.string.empty)
        RANDOM_WORD -> context.getString(R.string.random_word)
        RANDOM_CHARACTERS -> context.getString(R.string.random_characters)
    }

    fun generate(randomCharacterCount: Int) = when (this) {
        EMPTY -> ""
        RANDOM_WORD -> WordList.words.random()
        RANDOM_CHARACTERS -> {
            val chars = ('a'..'z') + ('0'..'9')
            (1..randomCharacterCount).map { chars.random() }.joinToString("")
        }
    }
}

enum class Theme {
    LIGHT, DARK, MATCH_SYSTEM;

    fun title(context: Context) = when (this) {
        LIGHT -> context.getString(R.string.light)
        DARK -> context.getString(R.string.dark)
        MATCH_SYSTEM -> context.getString(R.string.device_default)
    }
}