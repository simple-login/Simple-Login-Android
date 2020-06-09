package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AliasMailbox(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String
) : Parcelable