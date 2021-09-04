package io.simplelogin.android.utils.model

import android.database.Cursor
import android.provider.ContactsContract

data class PickedEmail(private val cursor: Cursor) {
    val description: String
    val address: String =
        cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA))

    init {
        val icon =
            when (cursor.getInt(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))) {
                2 -> "\uD83C\uDFE2" // Work
                3 -> "\uD83D\uDCE7" // Other
                4 -> "\uD83D\uDCF1" // Mobile
                else -> "\uD83C\uDFE0" // Home
            }
        description = "$icon $address"
    }
}