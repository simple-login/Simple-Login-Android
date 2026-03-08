package io.simplelogin.android.ui.home.settings.account

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import io.simplelogin.android.R
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.RandomAliasSuffix
import io.simplelogin.android.data.models.api.RandomMode
import io.simplelogin.android.data.models.api.SenderFormat
import io.simplelogin.android.data.models.api.UpdateUserInfoOption
import io.simplelogin.android.data.models.api.UpdateUserSettingsOption
import io.simplelogin.android.data.models.api.UsableDomain
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.api.UserSettings
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import io.simplelogin.android.usecases.session.UpdateSessionSettingsUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Base64
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val updateSessionSettings: UpdateSessionSettingsUseCase,
    private val datasource: AccountSettingsRemoteDatasource
) : ViewModel() {
    private var apiKey: ApiKey? = null
    private val _stateFlow = MutableStateFlow(AccountSettingsState.Default)
    val stateFlow: StateFlow<AccountSettingsState> = _stateFlow

    private val _informationStateFlow = MutableStateFlow<String?>(null)
    val informationStateFlow: StateFlow<String?> = _informationStateFlow

    fun refresh() {
        _stateFlow.update { AccountSettingsState.Default }
        withApiKey { apiKey ->
            coroutineScope {
                val userInfo = async { datasource.getUserInfo(apiKey) }
                val userSettings = async { datasource.getUserSettings(apiKey) }
                val usableDomains = async { datasource.getUsableDomains(apiKey) }
                handleResults(
                    userInfoResult = userInfo.await(),
                    userSettingsResult = userSettings.await(),
                    usableDomainsResult = usableDomains.await()
                )
            }
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

    private fun withApiKey(block: suspend (ApiKey) -> Unit) {
        apiKey?.let { viewModelScope.launch { block(it) } }
            ?: viewModelScope.launch {
                observeSessionSettings()
                    .mapNotNull { it.apiKey }
                    .collect { fetchedApiKey ->
                        apiKey = fetchedApiKey
                        block(fetchedApiKey)
                    }
            }
    }

    private fun handleError(error: ApiError) {
        _stateFlow.update {
            it.copy(isLoading = false, updateError = error)
        }
    }
}
