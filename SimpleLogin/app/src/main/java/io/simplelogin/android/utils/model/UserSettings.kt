package io.simplelogin.android.utils.model

import com.google.gson.annotations.SerializedName
import io.simplelogin.android.utils.enums.RandomMode
import io.simplelogin.android.utils.enums.SenderFormat

data class UserSettings(
    @SerializedName("alias_generator") val randomMode: RandomMode,
    @SerializedName("notification") val notification: Boolean,
    @SerializedName("random_alias_default_domain") val randomAliasDefaultDomain: String,
    @SerializedName("sender_format") val senderFormat: SenderFormat
) {
    sealed class Option {
        class RandomModeOption(val randomMode: RandomMode) : Option()
        class NotificationOption(val isOn: Boolean) : Option()
        class RandomAliasDefaultDomainOption(val domainName: String) : Option()
        class SenderFormatOption(val senderFormat: SenderFormat) : Option()

        val requestMap
            get() = when (this) {
                is RandomModeOption -> mapOf("alias_generator" to randomMode.parameterName)
                is NotificationOption -> mapOf("notification" to isOn)
                is RandomAliasDefaultDomainOption -> mapOf("random_alias_default_domain" to domainName)
                is SenderFormatOption -> mapOf("sender_format" to senderFormat.parameterName)
            }
    }
}