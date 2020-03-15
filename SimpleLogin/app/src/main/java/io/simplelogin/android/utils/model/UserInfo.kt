package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserInfo(
    @SerializedName("name") val name: String,
    @SerializedName("is_premium") val isPremium: Boolean
) : Parcelable