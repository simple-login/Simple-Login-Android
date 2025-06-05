package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class UpdateAliasOptions(
    @SerialName("note") val note: String? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("mailbox_ids") val mailboxIds: List<Int>? = null,
    @SerialName("disable_pgp") val disablePgp: Boolean? = null,
    @SerialName("pinned") val pinned: Boolean? = null
)
