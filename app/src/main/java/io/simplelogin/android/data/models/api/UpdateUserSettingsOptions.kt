package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class UpdateUserSettingsOptions(
    @SerializedName("alias_generator") val randomMode: RandomMode? = null,
    @SerializedName("notification") val notification: Boolean? = null,
    @SerializedName("random_alias_default_domain") val randomAliasDefaultDomain: String? = null,
    @SerializedName("sender_format") val senderFormat: SenderFormat? = null,
    @SerializedName("random_alias_suffix") val randomAliasSuffix: RandomAliasSuffix? = null
)