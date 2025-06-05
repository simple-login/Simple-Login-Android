package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class UpdateAliasOptions(
    @SerialName("note") val note: String?,
    @SerialName("name") val name: String?,
    @SerialName("mailbox_ids") val mailboxIds: List<Int>?,
    @SerialName("disable_pgp") val disablePgp: Boolean?,
    @SerialName("pinned") val pinned: Boolean?
)
