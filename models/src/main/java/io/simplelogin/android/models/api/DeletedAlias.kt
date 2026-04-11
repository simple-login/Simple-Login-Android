package io.simplelogin.android.models.api

import com.google.gson.annotations.SerializedName

data class DeletedAlias(
    @SerializedName("alias") val alias: String,
    @SerializedName("deletion_timestamp") val deletionTimestamp: Double
)

data class DeletedAliases(
    @SerializedName("aliases") val value: List<DeletedAlias>
)