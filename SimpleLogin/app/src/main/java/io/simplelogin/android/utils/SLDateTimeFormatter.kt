package io.simplelogin.android.utils

import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

object SLDateTimeFormatter {
    private val preciseFormatter = SimpleDateFormat("d MMM yyyy 'at' HH:mm")
    fun preciseCreationDateStringFrom(seconds: Long, prefix: String? = null) : String {
        val preciseDateAndTimeString = preciseFormatter.format(Date(TimeUnit.SECONDS.toMillis(seconds)))
        val distance = distanceFromNow(seconds)
        return if (prefix != null) {
            "$prefix $preciseDateAndTimeString (${distance.first} ${distance.second} ago)"
        } else {
            "$preciseDateAndTimeString (${distance.first} ${distance.second} ago)"
        }
    }

    @Suppress("MagicNumber", "ReturnCount")
    fun distanceFromNow(seconds: Long) : Pair<Int, String> {
        val nowSeconds = TimeUnit.MILLISECONDS.toSeconds(Date().time)
        val secondsGap = nowSeconds - seconds

        val days = TimeUnit.SECONDS.toDays(secondsGap).toInt()

        // MONTH
        val months = days / 30
        if (months == 1) {
            return Pair(1, "month")
        } else if (months > 1) {
            return Pair(months, "months")
        }

        // WEEK
        val weeks = days / 7
        if (weeks == 1) {
            return Pair(1, "week")
        } else if (weeks > 1) {
            return Pair(weeks, "weeks")
        }

        // DAY
        if (days == 1) {
            return Pair(1, "day")
        } else if (days > 1) {
            return Pair(days, "days")
        }

        // HOUR
        val hours = TimeUnit.SECONDS.toHours(secondsGap).toInt()
        if (hours == 1) {
            return Pair(1, "hour")
        } else if (hours > 1) {
            return Pair(hours, "hours")
        }

        // MINUTES
        val minutes = TimeUnit.SECONDS.toMinutes(secondsGap).toInt()
        return if (minutes == 1) {
            Pair(1, "minute")
        } else {
            Pair(minutes, "minutes")
        }
    }
}
