package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class ApiKey(
    @SerializedName("api_key") val value: String
) : Parcelable
