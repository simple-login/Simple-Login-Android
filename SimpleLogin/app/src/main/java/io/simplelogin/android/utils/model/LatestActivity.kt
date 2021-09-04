package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class LatestActivity(
    @SerializedName("action") val action: Action,
    @SerializedName("contact") val contact: ContactLite,
    @SerializedName("timestamp") val timestamp: Long
): Parcelable
