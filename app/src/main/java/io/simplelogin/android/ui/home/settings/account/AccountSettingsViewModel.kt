package io.simplelogin.android.ui.home.settings.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.simplelogin.android.data.models.api.ApiError
import io.simplelogin.android.data.models.api.ApiKey
import io.simplelogin.android.data.models.api.UpdateUserSettingsOptions
import io.simplelogin.android.data.models.api.UserInfo
import io.simplelogin.android.data.models.api.UserSettings
import io.simplelogin.android.data.remote.datasource.AccountSettingsRemoteDatasource
import io.simplelogin.android.data.util.Result
import io.simplelogin.android.usecases.session.ObserveSessionSettingsUseCase
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AccountSettingsViewModel @Inject constructor(
    private val observeSessionSettings: ObserveSessionSettingsUseCase,
    private val datasource: AccountSettingsRemoteDatasource
) : ViewModel() {
    private var apiKey: ApiKey? = null
    private val _stateFlow = MutableStateFlow(AccountSettingsState.Default)
    val stateFlow: StateFlow<AccountSettingsState> = _stateFlow

    init {
        viewModelScope.launch {
            refresh()
        }
    }

    suspend fun refresh() {
        _stateFlow.update { AccountSettingsState.Default }
        withApiKey { apiKey ->
            coroutineScope {
                val userInfo = async { datasource.getUserInfo(apiKey = apiKey) }
                val userSettings = async { datasource.getUserSettings(apiKey = apiKey) }
                handleResults(
                    userInfoResult = userInfo.await(),
                    userSettingsResult = userSettings.await()
                )
            }
        }
    }

    private fun handleResults(
        userInfoResult: Result<UserInfo, ApiError>,
        userSettingsResult: Result<UserSettings, ApiError>
    ) {
        when {
            userInfoResult is Result.Success && userSettingsResult is Result.Success -> {
                _stateFlow.update {
                    it.copy(
                        settings = AccountSettings(
                            userInfo = userInfoResult.value,
                            userSettings = userSettingsResult.value
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

    fun updateNotification(notification: Boolean) {
        withApiKey { apiKey ->
            _stateFlow.update { it.copy(isLoading = true) }
            val settings = datasource.updateUserSettings(
                apiKey = apiKey,
                options = UpdateUserSettingsOptions(notification = notification)
            )
            when (settings) {
                is Result.Success -> _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        settings = it.settings?.copy(userSettings = settings.value)
                    )
                }

                is Result.Failure -> _stateFlow.update {
                    it.copy(
                        isLoading = false,
                        updateError = settings.error
                    )
                }
            }
        }
    }

    private fun withApiKey(block: suspend (ApiKey) -> Unit) {
        apiKey?.let { viewModelScope.launch { block(it) } }
            ?: viewModelScope.launch {
                observeSessionSettings()
                    .mapNotNull { it.apiKey }
                    .first()
                    .let { fetchedApiKey ->
                        apiKey = fetchedApiKey
                        block(fetchedApiKey)
                    }
            }
    }
}