package io.simplelogin.android.core.model.api

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable

@Serializable
data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_picture_url") val profilePictureUrl: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("in_trial") val inTrial: Boolean,
    @SerializedName("trial_end_timestamp") val trialEndTimestamp: Double?,
    @SerializedName("max_alias_free_plan") val maxAliasFreePlan: Int,
    @SerializedName("connected_proton_address") val connectedProtonAddress: String?,
    @SerializedName("can_create_reverse_alias") val canCreateReverseAlias: Boolean
) {
    val initial: Char?
        get() = (name.firstOrNull() ?: email.firstOrNull())?.uppercaseChar()

    val isPremiumOrTrial: Boolean
        get() = isPremium || inTrial
}
