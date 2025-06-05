package io.simplelogin.android.data.models

import kotlinx.serialization.SerialName

data class UpdateCustomDomainOptions(
    @SerialName("catch_all") val catchAll: Boolean? = null,
    @SerialName("random_prefix_generation") val randomPrefixGeneration: Boolean? = null,
    @SerialName("name") val name: String? = null,
    @SerialName("mailbox_ids") val mailboxIds: List<Int>? = null
)

data class UpdateCustomDomainResponse(
    @SerialName("custom_domain") val customDomain: CustomDomain
)