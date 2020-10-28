package io.simplelogin.android.utils.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AliasMailbox(
    @SerializedName("id") val id: Int,
    @SerializedName("email") val email: String
) : Parcelable, Comparable<AliasMailbox> {
    override fun compareTo(other: AliasMailbox): Int = this.email.compareTo(other.email)
}
