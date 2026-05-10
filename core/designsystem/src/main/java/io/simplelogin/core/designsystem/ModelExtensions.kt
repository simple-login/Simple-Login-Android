package io.simplelogin.core.designsystem

import android.content.Context
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.RandomAliasSuffix
import io.simplelogin.core.model.api.RandomAliasSuffix.RANDOM_STRING
import io.simplelogin.core.model.api.RandomMode
import io.simplelogin.core.model.api.RandomMode.UUID
import io.simplelogin.core.model.api.RandomMode.WORD
import io.simplelogin.core.model.api.SenderFormat
import io.simplelogin.core.model.api.SenderFormat.A
import io.simplelogin.core.model.api.SenderFormat.AT
import io.simplelogin.core.model.api.SenderFormat.AT_ONLY
import io.simplelogin.core.model.api.SenderFormat.NAME_ONLY
import io.simplelogin.core.model.api.SenderFormat.NO_NAME
import io.simplelogin.core.model.preferences.AliasCellSelection
import io.simplelogin.core.model.preferences.AliasCellSelection.COPY_EMAIL
import io.simplelogin.core.model.preferences.AliasCellSelection.VIEW_DETAILS
import io.simplelogin.core.model.preferences.AliasCellSelection.VIEW_OPTIONS
import io.simplelogin.core.model.preferences.AliasDisplayInfo
import io.simplelogin.core.model.preferences.AliasDisplayInfo.CREATION_DATE
import io.simplelogin.core.model.preferences.AliasDisplayInfo.LAST_14_DAYS
import io.simplelogin.core.model.preferences.AliasDisplayInfo.LATEST_ACTIVITY
import io.simplelogin.core.model.preferences.AliasDisplayInfo.MAILBOXES
import io.simplelogin.core.model.preferences.AliasDisplayInfo.NOTE
import io.simplelogin.core.model.preferences.AliasOptionsDisplay
import io.simplelogin.core.model.preferences.AliasOptionsDisplay.BOTTOM_SHEET
import io.simplelogin.core.model.preferences.AliasOptionsDisplay.DROPDOWN_MENU
import io.simplelogin.core.model.preferences.ContactCellSelection
import io.simplelogin.core.model.preferences.ContactCellSelection.BLOCK_UNBLOCK
import io.simplelogin.core.model.preferences.ContactCellSelection.COPY_ADDRESS
import io.simplelogin.core.model.preferences.ContactCellSelection.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME
import io.simplelogin.core.model.preferences.ContactCellSelection.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME
import io.simplelogin.core.model.preferences.ContactCellSelection.OPEN_DEFAULT_EMAIL_CLIENT
import io.simplelogin.core.model.preferences.DefaultPrefix
import io.simplelogin.core.model.preferences.DefaultPrefix.EMPTY
import io.simplelogin.core.model.preferences.DefaultPrefix.RANDOM_CHARACTERS
import io.simplelogin.core.model.preferences.DefaultPrefix.RANDOM_WORD
import io.simplelogin.core.model.preferences.DeviceLockType
import io.simplelogin.core.model.preferences.DeviceLockType.BIOMETRIC
import io.simplelogin.core.model.preferences.DeviceLockType.NONE
import io.simplelogin.core.model.preferences.DeviceLockType.PIN
import io.simplelogin.core.model.preferences.LockTimeOut
import io.simplelogin.core.model.preferences.LockTimeOut.FIVE_MINUTES
import io.simplelogin.core.model.preferences.LockTimeOut.IMMEDIATE
import io.simplelogin.core.model.preferences.LockTimeOut.ONE_HOUR
import io.simplelogin.core.model.preferences.LockTimeOut.ONE_MINUTE
import io.simplelogin.core.model.preferences.LockTimeOut.TEN_MINUTES
import io.simplelogin.core.model.preferences.LockTimeOut.TWO_MINUTES
import io.simplelogin.core.model.preferences.SwipeAction
import io.simplelogin.core.model.preferences.SwipeAction.DELETE
import io.simplelogin.core.model.preferences.SwipeAction.DISABLE_ENABLE
import io.simplelogin.core.model.preferences.SwipeAction.PIN_UNPIN
import io.simplelogin.core.model.preferences.Theme
import io.simplelogin.core.model.preferences.Theme.DARK
import io.simplelogin.core.model.preferences.Theme.LIGHT
import io.simplelogin.core.model.preferences.Theme.MATCH_SYSTEM
import io.simplelogin.core.model.ui.AliasFilterMode

fun ApiError.description(context: Context): String = when (this) {
    is ApiError.HttpError -> {
        if (errorMessage != null) {
            return errorMessage + " ($code)"
        }
        when (this.code) {
            429 -> context.getString(R.string.too_many_requests)
            in 400..499 -> context.getString(R.string.client_error, this.code)
            in 500..599 -> context.getString(R.string.internal_server_error, this.code)
            else -> context.getString(R.string.unknown_http_error, this.code)
        }
    }

    is ApiError.UnknownError -> context.getString(R.string.generic_error, e.toString())
}

fun RandomMode.title(context: Context) = when (this) {
    UUID -> "UUID"
    WORD -> context.getString(R.string.random_word)
}

fun RandomAliasSuffix.title(context: Context): String =
    when (this) {
        RandomAliasSuffix.WORD -> context.getString(R.string.random_word)
        RANDOM_STRING -> context.getString(R.string.random_5_characters)
    }

fun SenderFormat.description(context: Context): String =
    when (this) {
        A -> "John Doe - john.doe(a)example.com"
        AT -> "John Doe - john.doe at example.com"
        NAME_ONLY -> "John Doe"
        AT_ONLY -> "john at example.com"
        NO_NAME -> context.getString(R.string.no_name_format_description)
    }

fun AliasCellSelection.title(context: Context) = when (this) {
    VIEW_DETAILS -> context.getString(R.string.view_details)
    COPY_EMAIL -> context.getString(R.string.copy_alias_address)
    VIEW_OPTIONS -> context.getString(R.string.view_options)
}

fun AliasOptionsDisplay.title(context: Context) = when (this) {
    BOTTOM_SHEET -> context.getString(R.string.bottom_sheet)
    DROPDOWN_MENU -> context.getString(R.string.drop_down_menu)
}

fun SwipeAction.title(context: Context) = when (this) {
    DISABLE_ENABLE -> context.getString(R.string.disable_enable)
    PIN_UNPIN -> context.getString(R.string.pin_unpin)
    DELETE -> context.getString(R.string.delete)
}

fun AliasDisplayInfo.title(context: Context) = when (this) {
    CREATION_DATE -> context.getString(R.string.creation_date)
    LATEST_ACTIVITY -> context.getString(R.string.latest_activity)
    NOTE -> context.getString(R.string.note)
    MAILBOXES -> context.getString(R.string.mailboxes)
    LAST_14_DAYS -> context.getString(R.string.last_14_days)
}

fun DefaultPrefix.title(context: Context) = when (this) {
    EMPTY -> context.getString(R.string.empty)
    RANDOM_WORD -> context.getString(R.string.random_word)
    RANDOM_CHARACTERS -> context.getString(R.string.random_characters)
}

fun Theme.title(context: Context) = when (this) {
    LIGHT -> context.getString(R.string.light)
    DARK -> context.getString(R.string.dark)
    MATCH_SYSTEM -> context.getString(R.string.device_default)
}

fun ContactCellSelection.title(context: Context) = when (this) {
    COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME ->
        context.getString(R.string.copy_reverse_alias_with_display_name)

    COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME ->
        context.getString(R.string.copy_reverse_alias_without_display_name)

    COPY_ADDRESS -> context.getString(R.string.copy_email_address)
    BLOCK_UNBLOCK -> context.getString(R.string.block_unblock)
    OPEN_DEFAULT_EMAIL_CLIENT -> context.getString(R.string.open_default_email_client)
    ContactCellSelection.VIEW_OPTIONS -> context.getString(R.string.view_options)
}

fun DeviceLockType.title(context: Context) = when (this) {
    NONE -> context.getString(R.string.none)
    BIOMETRIC -> context.getString(R.string.biometric)
    PIN -> context.getString(R.string.pin_code)
}

fun LockTimeOut.title(context: Context) = when (this) {
    IMMEDIATE -> context.getString(R.string.immediately)
    ONE_MINUTE -> context.getString(R.string.after_one_minute)
    TWO_MINUTES -> context.getString(R.string.after_two_minutes)
    FIVE_MINUTES -> context.getString(R.string.after_five_minutes)
    TEN_MINUTES -> context.getString(R.string.after_ten_minutes)
    ONE_HOUR -> context.getString(R.string.after_one_hour)
}

fun AliasFilterMode.title(context: Context) =
    when (this) {
        AliasFilterMode.ALL -> context.getString(R.string.all_aliases)
        AliasFilterMode.PINNED -> context.getString(R.string.pinned_aliases)
        AliasFilterMode.ENABLED -> context.getString(R.string.active_aliases)
        AliasFilterMode.DISABLED -> context.getString(R.string.disabled_aliases)
    }

fun AliasFilterMode.noAliasesMessage(context: Context) =
    when (this) {
        AliasFilterMode.ALL -> context.getString(R.string.no_aliases)
        AliasFilterMode.PINNED -> context.getString(R.string.no_pinned_aliases)
        AliasFilterMode.ENABLED -> context.getString(R.string.no_active_aliases)
        AliasFilterMode.DISABLED -> context.getString(R.string.no_disabled_aliases)
    }
