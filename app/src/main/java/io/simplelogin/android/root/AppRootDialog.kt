package io.simplelogin.android.root

import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.CustomDomain

sealed interface AppRootDialog {
    data object LogOut : AppRootDialog
    data object DeviceSettings : AppRootDialog
    data class AccountSettings(val apiKey: ApiKey) : AppRootDialog
    data class Mailboxes(val apiKey: ApiKey) : AppRootDialog
    data class CustomDomains(val apiKey: ApiKey) : AppRootDialog
    data class CustomDomainDetails(val apiKey: ApiKey, val domain: CustomDomain) : AppRootDialog
    data class CustomDomainDeletedAliases(val apiKey: ApiKey, val domain: CustomDomain) :
        AppRootDialog
}
