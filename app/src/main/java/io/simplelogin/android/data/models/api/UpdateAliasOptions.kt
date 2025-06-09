package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class UpdateAliasOptions(
    @SerializedName("note") val note: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mailbox_ids") val mailboxIds: List<Int>? = null,
    @SerializedName("disable_pgp") val disablePgp: Boolean? = null,
    @SerializedName("pinned") val pinned: Boolean? = null
)
