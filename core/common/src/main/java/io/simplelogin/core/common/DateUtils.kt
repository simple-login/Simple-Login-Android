package io.simplelogin.core.common

import android.content.Context
import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun Double.relativeDateTime(context: Context): String =
    DateUtils.getRelativeDateTimeString(
        context,
        (this * 1_000).toLong(),
        DateUtils.MINUTE_IN_MILLIS,
        DateUtils.WEEK_IN_MILLIS,
        DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_TIME or
                DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_ABBREV_RELATIVE or
                DateUtils.FORMAT_ABBREV_MONTH
    ).toString()

fun Double.timeAndFullDate(): String {
    val dateFormat = SimpleDateFormat("HH:mm MMMM d, yyyy", Locale.getDefault())
    return dateFormat.format(Date((this * 1_000).toLong()))
}

fun Double.relativeTimeSpan(): String =
    DateUtils.getRelativeTimeSpanString(
        (this * 1_000).toLong(),
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()