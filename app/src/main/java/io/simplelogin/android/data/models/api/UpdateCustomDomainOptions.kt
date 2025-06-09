package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class UpdateCustomDomainOptions(
    @SerializedName("catch_all") val catchAll: Boolean? = null,
    @SerializedName("random_prefix_generation") val randomPrefixGeneration: Boolean? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("mailbox_ids") val mailboxIds: List<Int>? = null
)

data class UpdateCustomDomainResponse(
    @SerializedName("custom_domain") val customDomain: CustomDomain
)