package io.simplelogin.core.model.preferences

import kotlinx.serialization.Serializable

@Serializable
data class DevicePreferences(
    val theme: Theme = Theme.MATCH_SYSTEM,
    val dynamicColor: Boolean = false,
    val showStats: Boolean = true,
    val copyAfterCreating: Boolean = true,
    val askForRandomAliasNote: Boolean = false,
    val aliasCellSelection: AliasCellSelection = AliasCellSelection.Default,
    val aliasOptionsDisplay: AliasOptionsDisplay = AliasOptionsDisplay.Default,
    val swipeFromLeftToRightAction: SwipeAction = SwipeAction.DISABLE_ENABLE,
    val swipeFromRightToLeftAction: SwipeAction = SwipeAction.PIN_UNPIN,
    val aliasDisplayInfos: List<AliasDisplayInfo> = AliasDisplayInfo.entries.toTypedArray()
        .toList(),
    val defaultPrefix: DefaultPrefix = DefaultPrefix.RANDOM_WORD,
    val prefixRandomCharacterCount: Int = 5,
    val contactCellSelection: ContactCellSelection = ContactCellSelection.Default
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
}

enum class AliasOptionsDisplay {
    BOTTOM_SHEET, DROPDOWN_MENU;

    companion object {
        val Default = BOTTOM_SHEET
    }
}

enum class SwipeAction {
    NONE, DISABLE_ENABLE, PIN_UNPIN, DELETE
}

enum class AliasDisplayInfo {
    CREATION_DATE, LATEST_ACTIVITY, NOTE, MAILBOXES, LAST_14_DAYS
}

enum class DefaultPrefix {
    EMPTY, RANDOM_WORD, RANDOM_CHARACTERS;

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
    LIGHT, DARK, MATCH_SYSTEM
}

enum class ContactCellSelection {
    COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME,
    COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME,
    COPY_ADDRESS,
    BLOCK_UNBLOCK,
    OPEN_DEFAULT_EMAIL_CLIENT,
    VIEW_OPTIONS;

    companion object {
        val Default = VIEW_OPTIONS
    }
}
