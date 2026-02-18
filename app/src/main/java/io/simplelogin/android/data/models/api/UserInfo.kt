package io.simplelogin.android.data.models.api

import com.google.gson.annotations.SerializedName

data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_picture_url") val profilePictureUrl: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("in_trial") val inTrial: Boolean,
    @SerializedName("max_alias_free_plan") val maxAliasFreePlan: Int,
    @SerializedName("connected_proton_address") val connectedProtonAddress: String?,
    @SerializedName("can_create_reverse_alias") val canCreateReverseAlias: Boolean
) {
    val initial: Char?
        get() = (name.firstOrNull() ?: email.firstOrNull())?.uppercaseChar()
}
