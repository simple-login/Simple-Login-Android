package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("profile_picture_url") val profilePhotoUrl: String?,
    @SerializedName("is_premium") val isPremium: Boolean,
    @SerializedName("in_trial") val inTrial: Boolean,
    @SerializedName("connected_proton_address") val connectedProtonAddress: String?
) : Parcelable
