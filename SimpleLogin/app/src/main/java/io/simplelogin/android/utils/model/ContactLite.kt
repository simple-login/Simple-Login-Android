package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ContactLite(
    @SerializedName("email") val email: String,
    @SerializedName("name") val name: String?,
    @SerializedName("reverse_alias") val reverseAlias: String
) : Parcelable
