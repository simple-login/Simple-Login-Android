package io.simplelogin.android.designsystem

import android.content.Context
import io.simplelogin.android.models.api.ApiError
import io.simplelogin.android.models.api.RandomAliasSuffix
import io.simplelogin.android.models.api.RandomAliasSuffix.RANDOM_STRING
import io.simplelogin.android.models.api.RandomMode
import io.simplelogin.android.models.api.RandomMode.UUID
import io.simplelogin.android.models.api.RandomMode.WORD
import io.simplelogin.android.models.api.SenderFormat
import io.simplelogin.android.models.api.SenderFormat.A
import io.simplelogin.android.models.api.SenderFormat.AT
import io.simplelogin.android.models.api.SenderFormat.AT_ONLY
import io.simplelogin.android.models.api.SenderFormat.NAME_ONLY
import io.simplelogin.android.models.api.SenderFormat.NO_NAME
import io.simplelogin.android.models.preferences.AliasCellSelection
import io.simplelogin.android.models.preferences.AliasCellSelection.COPY_EMAIL
import io.simplelogin.android.models.preferences.AliasCellSelection.VIEW_DETAILS
import io.simplelogin.android.models.preferences.AliasCellSelection.VIEW_OPTIONS
import io.simplelogin.android.models.preferences.AliasDisplayInfo
import io.simplelogin.android.models.preferences.AliasDisplayInfo.CREATION_DATE
import io.simplelogin.android.models.preferences.AliasDisplayInfo.LAST_14_DAYS
import io.simplelogin.android.models.preferences.AliasDisplayInfo.LATEST_ACTIVITY
import io.simplelogin.android.models.preferences.AliasDisplayInfo.MAILBOXES
import io.simplelogin.android.models.preferences.AliasDisplayInfo.NOTE
import io.simplelogin.android.models.preferences.AliasOptionsDisplay
import io.simplelogin.android.models.preferences.AliasOptionsDisplay.BOTTOM_SHEET
import io.simplelogin.android.models.preferences.AliasOptionsDisplay.DROPDOWN_MENU
import io.simplelogin.android.models.preferences.ContactCellSelection
import io.simplelogin.android.models.preferences.ContactCellSelection.BLOCK_UNBLOCK
import io.simplelogin.android.models.preferences.ContactCellSelection.COPY_ADDRESS
import io.simplelogin.android.models.preferences.ContactCellSelection.COPY_REVERSE_ALIAS_WITHOUT_DISPLAY_NAME
import io.simplelogin.android.models.preferences.ContactCellSelection.COPY_REVERSE_ALIAS_WITH_DISPLAY_NAME
import io.simplelogin.android.models.preferences.ContactCellSelection.OPEN_DEFAULT_EMAIL_CLIENT
import io.simplelogin.android.models.preferences.DefaultPrefix
import io.simplelogin.android.models.preferences.DefaultPrefix.EMPTY
import io.simplelogin.android.models.preferences.DefaultPrefix.RANDOM_CHARACTERS
import io.simplelogin.android.models.preferences.DefaultPrefix.RANDOM_WORD
import io.simplelogin.android.models.preferences.DeviceLockType
import io.simplelogin.android.models.preferences.DeviceLockType.BIOMETRIC
import io.simplelogin.android.models.preferences.DeviceLockType.NONE
import io.simplelogin.android.models.preferences.DeviceLockType.PIN
import io.simplelogin.android.models.preferences.LockTimeOut
import io.simplelogin.android.models.preferences.LockTimeOut.FIVE_MINUTES
import io.simplelogin.android.models.preferences.LockTimeOut.IMMEDIATE
import io.simplelogin.android.models.preferences.LockTimeOut.ONE_HOUR
import io.simplelogin.android.models.preferences.LockTimeOut.ONE_MINUTE
import io.simplelogin.android.models.preferences.LockTimeOut.TEN_MINUTES
import io.simplelogin.android.models.preferences.LockTimeOut.TWO_MINUTES
import io.simplelogin.android.models.preferences.SwipeAction
import io.simplelogin.android.models.preferences.SwipeAction.DELETE
import io.simplelogin.android.models.preferences.SwipeAction.DISABLE_ENABLE
import io.simplelogin.android.models.preferences.SwipeAction.PIN_UNPIN
import io.simplelogin.android.models.preferences.Theme
import io.simplelogin.android.models.preferences.Theme.DARK
import io.simplelogin.android.models.preferences.Theme.LIGHT
import io.simplelogin.android.models.preferences.Theme.MATCH_SYSTEM

fun ApiError.description(context: Context): String = when (this) {
    is ApiError.HttpError -> {
        if (errorMessage != null) {
            return errorMessage + " (${code})"
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