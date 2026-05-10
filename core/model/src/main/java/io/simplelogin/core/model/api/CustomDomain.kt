package io.simplelogin.core.model.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class CustomDomain(
    @SerializedName("id") val id: Int,
    @SerializedName("creation_timestamp") val creationTimestamp: Double,
    @SerializedName("domain_name") val domainName: String,
    @SerializedName("name") val name: String?,
    @SerializedName("is_verified") val isVerified: Boolean,
    @SerializedName("nb_alias") val aliasCount: Int,
    @SerializedName("random_prefix_generation") val randomPrefixGeneration: Boolean,
    @SerializedName("mailboxes") val mailboxes: List<MailboxLite>,
    @SerializedName("catch_all") val catchAll: Boolean
)

data class CustomDomains(
    @SerializedName("custom_domains") val value: List<CustomDomain>
)
