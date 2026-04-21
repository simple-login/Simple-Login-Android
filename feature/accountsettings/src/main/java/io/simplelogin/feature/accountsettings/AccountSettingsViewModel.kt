package io.simplelogin.feature.accountsettings

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.core.common.ProtonLinkManager
import io.simplelogin.core.common.usecase.UpdateSessionSettingsUseCase
import io.simplelogin.core.model.Result
import io.simplelogin.core.model.api.ApiError
import io.simplelogin.core.model.api.ApiKey
import io.simplelogin.core.model.api.RandomAliasSuffix
import io.simplelogin.core.model.api.RandomMode
import io.simplelogin.core.model.api.SenderFormat
import io.simplelogin.core.model.api.UpdateUserInfoOption
import io.simplelogin.core.model.api.UpdateUserSettingsOption
import io.simplelogin.core.model.api.UsableDomain
import io.simplelogin.core.model.api.UserInfo
import io.simplelogin.core.model.api.UserSettings
import io.simplelogin.core.network.BaseUrlProvider
import io.simplelogin.core.network.datasource.AccountSettingsRemoteDatasource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Base64

@HiltViewModel(assistedFactory = AccountSettingsViewModel.Factory::class)
class AccountSettingsViewModel @AssistedInject constructor(
    @ApplicationContext private val context: Context,
    @Assisted private val apiKeyValue: String,
    private val updateSessionSettings: UpdateSessionSettingsUseCase,
    private val datasource: AccountSettingsRemoteDatasource,
    private val baseUrlProvider: BaseUrlProvider,
    private val protonLinkManager: ProtonLinkManager
) : ViewModel() {

    @AssistedFactory
    interface Factory {
        fun create(apiKeyValue: String): AccountSettingsViewModel
    }

    private val _stateFlow = MutableStateFlow(AccountSettingsState.Default)
    val stateFlow: StateFlow<AccountSettingsState> = _stateFlow

    private val _informationStateFlow = MutableStateFlow<String?>(null)
    val informationStateFlow: StateFlow<String?> = _informationStateFlow

    init {
        viewModelScope.launch {
            protonLinkManager.linkedEvents.collect {
                refresh()
                _informationStateFlow.value = context.getString(R.string.proton_account_linked)
            }
        }
    }

    fun refresh() {
        _stateFlow.update { AccountSettingsState.Default }
        withApiKey { apiKey ->
            val userInfo = async(Dispatchers.IO) { datasource.getUserInfo(apiKey) }
            val userSettings = async(Dispatchers.IO) { datasource.getUserSettings(apiKey) }
            val usableDomains = async(Dispatchers.IO) { datasource.getUsableDomains(apiKey) }
            handleResults(
                userInfoResult = userInfo.await(),
                userSettingsResult = userSettings.await(),
                usableDomainsResult = usableDomains.await()
            )
        }
    }

    private suspend fun handleResults(
        userInfoResult: Result<UserInfo, ApiError>,
        userSettingsResult: Result<UserSettings, ApiError>,
        usableDomainsResult: Result<List<UsableDomain>, ApiError>
    ) {
        when {
            userInfoResult is Result.Success &&
                    userSettingsResult is Result.Success &&
                    usableDomainsResult is Result.Success -> {
                val sortedUsableDomains = usableDomainsResult.value.sortedWith(
                    compareByDescending { it.isCustom }
                )
                updateSessionSettings { it.copy(userInfo = userInfoResult.value) }
                _stateFlow.update {
                    it.copy(
                        settings = AccountSettings(
                            userInfo = userInfoResult.value,
                            userSettings = userSettingsResult.value,
                            usableDomains = sortedUsableDomains
                        ),
                        isLoading = false,
                        fetchError = null
                    )
                }
            }

            userInfoResult is Result.Failure ->
                _stateFlow.update {
                    it.copy(isLoading = false, fetchError = userInfoResult.error)
                }

            userSettingsResult is Result.Failure ->
                _stateFlow.update {
                    it.copy(isLoading = false, fetchError = userSettingsResult.error)
                }
        }
    }

    fun clearUpdateError() {
        _stateFlow.update {
            it.copy(updateError = null)
        }
    }

    fun updateDisplayName(displayName: String) {
        updateInfo(UpdateUserInfoOption.DisplayName(displayName))
    }

    fun removeProfilePicture() {
        updateInfo(UpdateUserInfoOption.ProfilePicture(null))
    }

    fun updateProfilePicture(uri: Uri?, context: Context) {
        if (uri == null) return
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bytes = inputStream?.readBytes()
                inputStream?.close()

                if (bytes != null) {
                    val base64 = Base64.getEncoder().encodeToString(bytes)
                    updateInfo(UpdateUserInfoOption.ProfilePicture(base64))
                }
            } catch (e: Exception) {

            }
        }
    }

    fun updateNotification(notification: Boolean) {
        updateSettings(UpdateUserSettingsOption.Notification(notification))
    }

    fun updateRandomMode(mode: RandomMode) {
        updateSettings(UpdateUserSettingsOption.RandomModeOption(mode))
    }

    fun updateRandomAliasSuffix(suffix: RandomAliasSuffix) {
        updateSettings(UpdateUserSettingsOption.RandomAliasSuffixOption(suffix))
    }

    fun updateUsableDomain(domain: UsableDomain) {
        updateSettings(UpdateUserSettingsOption.RandomAliasDefaultDomain(domain.name))
    }

    fun updateSenderFormat(format: SenderFormat) {
        updateSettings(UpdateUserSettingsOption.SenderFormatOption(format))
    }

    fun linkProton() {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isLoading = true) }
            datasource.getTemporaryToken(apiKey = apiKey)
                .fold(onSuccess = { token ->
                    _stateFlow.update { it.copy(isLoading = false) }
                    val baseUrl = baseUrlProvider.getBaseUrl()
                    val scheme = context.getString(R.string.simplelogin_scheme)
                    val nextQuery = "/auth/proton/login?action=link&next=/link&scheme=$scheme"
                    val nextQueryEncoded = Uri.encode(nextQuery)
                    val url =
                        "$baseUrl/auth/api_to_cookie?token=${token.value}&next=$nextQueryEncoded"
                    val customTabsIntent = CustomTabsIntent.Builder().build()
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    customTabsIntent.launchUrl(context, url.toUri())
                }, onFailure = ::handleError)
        }
    }

    fun unlinkProton() {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isLoading = true) }
            datasource.unlinkProton(apiKey = apiKey)
                .fold(onSuccess = {
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            settings = it.settings?.copy(
                                userInfo = it.settings.userInfo.copy(
                                    connectedProtonAddress = null
                                )
                            )
                        )
                    }
                    _informationStateFlow.value =
                        context.getString(R.string.proton_account_unlinked)
                }, onFailure = ::handleError)
        }
    }

    fun clearInformation() {
        _informationStateFlow.value = null
    }

    private fun updateSettings(options: UpdateUserSettingsOption) {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isLoading = true) }
            datasource.updateUserSettings(apiKey = apiKey, options = options)
                .fold(onSuccess = { result ->
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            settings = it.settings?.copy(userSettings = result)
                        )
                    }
                    _informationStateFlow.value = context.getString(R.string.settings_updated)
                }, onFailure = ::handleError)
        }
    }

    private fun updateInfo(option: UpdateUserInfoOption) {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isLoading = true) }
            datasource.updateUserInfo(apiKey = apiKey, option = option)
                .fold(onSuccess = { result ->
                    updateSessionSettings { it.copy(userInfo = result) }
                    _stateFlow.update {
                        it.copy(
                            isLoading = false,
                            settings = it.settings?.copy(userInfo = result)
                        )
                    }
                    _informationStateFlow.value = context.getString(R.string.information_updated)
                }, onFailure = ::handleError)
        }
    }

    private fun withApiKey(
        scope: CoroutineScope = viewModelScope,
        block: suspend CoroutineScope.(ApiKey) -> Unit
    ) = scope.launch { block(ApiKey(apiKeyValue)) }


    private fun handleError(error: ApiError) {
        _stateFlow.update {
            it.copy(isLoading = false, updateError = error)
        }
    }
}
