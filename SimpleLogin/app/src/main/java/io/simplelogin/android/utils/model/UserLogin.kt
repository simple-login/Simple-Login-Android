package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserLogin(
    @SerializedName("api_key") val apiKey: String?,
    @SerializedName("mfa_enabled") val mfaEnabled: Boolean,
    @SerializedName("mfa_key") val mfaKey: String?,
    @SerializedName("name") val name: String
) : Parcelable
